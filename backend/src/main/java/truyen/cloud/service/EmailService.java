package truyen.cloud.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp, String recipientName);
}
