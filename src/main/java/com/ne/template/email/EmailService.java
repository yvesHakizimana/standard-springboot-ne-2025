package com.ne.template.email;


import com.ne.template.auth.OtpType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;

    @Async
    public void sendAccountVerificationEmail(String to, String name, String otp){
        sendOtpEmail(to, name, otp, OtpType.VERIFY_ACCOUNT);
    }

    @Async
    public void sendResetPasswordOtp(String to, String name, String otp){
        sendOtpEmail(to, name, otp, OtpType.RESET_PASSWORD);
    }

    private void sendOtpEmail(String to, String name, String otp, OtpType otpType){
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("otp", otp);
            context.setVariable("companyName", "BNR Bank");
            context.setVariable("expirationTime", "10");
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED);
            String process = "";
            String templateName = "";

            switch(otpType) {
                case VERIFY_ACCOUNT -> {
                    templateName = OtpType.VERIFY_ACCOUNT.name().toLowerCase();
                    process = templateEngine.process(templateName, context);
                    helper.setSubject("Verify your account with - One time Password (OTP) Required.");
                }
                case RESET_PASSWORD -> {
                    templateName = OtpType.RESET_PASSWORD.name().toLowerCase();
                    process = templateEngine.process(templateName, context);
                    helper.setSubject("Reset your password with - One time Password (OTP) Required.");
                }
                default -> log.error("Invalid otp type detected!");
            }

            helper.setText(process, true);
            helper.setTo(to);
            helper.setFrom("noreply@bnr.com");
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Unable to send the email", e);
        }
    }


}
