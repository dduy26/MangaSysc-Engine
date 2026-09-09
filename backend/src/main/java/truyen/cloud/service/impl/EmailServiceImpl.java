package truyen.cloud.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import truyen.cloud.service.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@truyencloud.com}")
    private String fromEmail;

    @Async
    @Override
    public void sendOtpEmail(String toEmail, String otp, String recipientName) {
        String displayName = (recipientName != null && !recipientName.isBlank()) ? recipientName : "Độc giả";
        String htmlContent = buildOtpEmailTemplate(otp, displayName);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String sender = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "noreply@truyencloud.com";
            helper.setFrom(sender, "Truyện Cloud");
            helper.setTo(toEmail);
            helper.setSubject("🔒 [Truyện Cloud] Mã xác nhận đặt lại mật khẩu của bạn: " + otp);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("📧 [EmailService] Đã gửi mã OTP thành công tới: {}", toEmail);
        } catch (Exception e) {
            log.warn("⚠️ [EmailService] Không thể gửi email qua SMTP: {}. Mã OTP dự phòng: [{}] cho email {}", e.getMessage(), otp, toEmail);
        }
    }

    private String buildOtpEmailTemplate(String otp, String name) {
        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"margin: 0; padding: 0; background-color: #fce7f3; font-family: 'Segoe UI', Arial, sans-serif;\">"
                + "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"padding: 40px 10px;\">"
                + "<tr><td align=\"center\">"
                + "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 520px; background: #ffffff; border-radius: 20px; box-shadow: 0 10px 25px rgba(244, 114, 182, 0.2); overflow: hidden; border: 1px solid #fbcfe8;\">"
                + "  <tr>"
                + "    <td style=\"background: linear-gradient(135deg, #ec4899 0%, #f43f5e 100%); padding: 32px 24px; text-align: center;\">"
                + "      <h1 style=\"color: #ffffff; margin: 0; font-size: 26px; font-weight: 800; letter-spacing: 0.5px;\">🌸 Truyện Cloud</h1>"
                + "      <p style=\"color: #fdf2f8; margin: 6px 0 0 0; font-size: 13.5px;\">Thế giới Truyện Tranh Soft Pink & Manga Đỉnh Cao</p>"
                + "    </td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td style=\"padding: 32px 28px;\">"
                + "      <h2 style=\"color: #1f2937; font-size: 18px; margin: 0 0 14px 0;\">Xin chào <strong>" + name + "</strong>,</h2>"
                + "      <p style=\"color: #4b5563; font-size: 14.5px; line-height: 1.6; margin: 0 0 24px 0;\">"
                + "        Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản Truyện Cloud của bạn. Dưới đây là mã xác thực OTP của bạn:"
                + "      </p>"
                + "      <div style=\"background: #fff1f2; border: 2px dashed #f43f5e; border-radius: 14px; padding: 18px; text-align: center; margin-bottom: 24px;\">"
                + "        <span style=\"font-size: 34px; font-weight: 900; letter-spacing: 8px; color: #e11d48; font-family: monospace;\">" + otp + "</span>"
                + "      </div>"
                + "      <p style=\"color: #6b7280; font-size: 13.5px; line-height: 1.5; margin: 0 0 16px 0;\">"
                + "        ⏱️ Mã này có hiệu lực trong vòng <strong>5 phút</strong>. Tuyệt đối không chia sẻ mã này cho bất kỳ ai khác."
                + "      </p>"
                + "      <p style=\"color: #9ca3af; font-size: 12.5px; line-height: 1.5; margin: 0;\">"
                + "        Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này. Tài khoản của bạn vẫn được an toàn."
                + "      </p>"
                + "    </td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td style=\"background: #fff5f7; border-top: 1px solid #fce7f3; padding: 18px 24px; text-align: center;\">"
                + "      <p style=\"color: #9ca3af; font-size: 12px; margin: 0;\">© 2026 Truyện Cloud • Đọc truyện không giới hạn</p>"
                + "    </td>"
                + "  </tr>"
                + "</table>"
                + "</td></tr>"
                + "</table>"
                + "</body>"
                + "</html>";
    }
}
