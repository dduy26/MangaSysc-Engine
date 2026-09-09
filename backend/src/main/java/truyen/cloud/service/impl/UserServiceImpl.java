package truyen.cloud.service.impl;

import truyen.cloud.dtos.request.ForgotPasswordRequest;
import truyen.cloud.dtos.request.LoginRequest;
import truyen.cloud.dtos.request.RegisterRequest;
import truyen.cloud.dtos.request.ResetPasswordRequest;
import truyen.cloud.dtos.response.AuthResponse;
import truyen.cloud.dtos.response.UserResponse;
import truyen.cloud.exception.ResourceNotFoundException;
import truyen.cloud.mapper.UserMapper;
import truyen.cloud.model.User;
import truyen.cloud.repository.UserRepository;
import truyen.cloud.service.EmailService;
import truyen.cloud.service.UserService;
import truyen.cloud.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of("ROLE_USER"))
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        
        AuthResponse response = userMapper.toAuthResponse(user);
        response.setAccessToken(accessToken);
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Nhờ AuthenticationManager xác thực username & password (tự bắn lỗi nếu sai)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        // 2. Tìm user trong DB
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        // 3. Sinh Access Token
        String accessToken = jwtUtil.generateAccessToken(user.getUsername());

        // 4. Dùng Mapper chuyển từ User sang AuthResponse (hoặc dùng Builder)
        AuthResponse response = userMapper.toAuthResponse(user);
        response.setAccessToken(accessToken);
        return response;
    }

    @Override
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        return userMapper.toUserResponse(user);
    }

    @Override
    public void sendForgotPasswordOtp(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 1. Kiểm tra tài khoản có tồn tại không
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản nào liên kết với email này!"));

        // 2. Chống spam: Giới hạn 60 giây gửi lại 1 lần
        String cooldownKey = "FORGOT_COOLDOWN:" + email;
        Object cooldown = redisTemplate.opsForValue().get(cooldownKey);
        if (cooldown != null) {
            throw new IllegalArgumentException("Vui lòng đợi 60 giây trước khi yêu cầu gửi lại mã OTP mới!");
        }

        // 3. Sinh mã OTP 6 chữ số ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(1000000));

        // 4. Lưu OTP vào Redis với thời hạn 5 phút (300s)
        String otpKey = "FORGOT_OTP:" + email;
        redisTemplate.opsForValue().set(otpKey, otp, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(cooldownKey, "1", 60, TimeUnit.SECONDS);

        // 5. Gửi email bất đồng bộ qua SMTP
        emailService.sendOtpEmail(email, otp, user.getUsername());
        log.info("🔑 [ForgotPassword] Đã phát sinh mã OTP cho email {}", email);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String inputOtp = request.getOtp().trim();
        String newPassword = request.getNewPassword().trim();

        // 1. Kiểm tra mã OTP trong Redis
        String otpKey = "FORGOT_OTP:" + email;
        Object storedOtpObj = redisTemplate.opsForValue().get(otpKey);
        if (storedOtpObj == null) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn hoặc không tồn tại. Vui lòng yêu cầu gửi lại mã mới!");
        }

        String storedOtp = storedOtpObj.toString().trim();
        if (!storedOtp.equals(inputOtp)) {
            throw new IllegalArgumentException("Mã OTP không chính xác. Vui lòng kiểm tra lại!");
        }

        // 2. Tìm tài khoản người dùng
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản!"));

        // 3. Cập nhật mật khẩu mới (BCrypt)
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 4. Xóa OTP sau khi đổi mật khẩu thành công
        redisTemplate.delete(otpKey);
        redisTemplate.delete("FORGOT_COOLDOWN:" + email);
        log.info("✅ [ForgotPassword] Đã đặt lại mật khẩu thành công cho email {}", email);
    }
}