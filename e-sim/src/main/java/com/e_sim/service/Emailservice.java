package com.e_sim.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.e_sim.dto.request.EmailDetails;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Service
public class Emailservice {

    private final JavaMailSender javaMailSender;

    // @Value("${spring.mail.username}")
    // private String emailsender;

    public void sendEmail(EmailDetails emailDetails) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("novatech10@outlook.com");
            helper.setTo(emailDetails.getReciepinet());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(buildHtmlTemplate(emailDetails.getMessageBody()), true); 

            log.info("HTML Email sent to {}", emailDetails.getReciepinet());
            javaMailSender.send(message);

        } catch (MessagingException | MailException e) {
            throw new RuntimeException("Error sending HTML email", e);
        }
    }


    private String buildHtmlTemplate(String content) {
        return """
                <html>
                    <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                        <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 8px;">
                            <h2 style="color: #fa0d0dff; text-align: center;">MIALA</h2>
                            <p style="font-size: 14px; color: #555555;">%s</p>
                            <hr style="border: none; border-top: 1px solid #ddd;">
                            <p style="font-size: 12px; color: #888888; text-align: center;">
                                &copy; %d NOVA TECH. All rights reserved.
                            </p>
                        </div>
                    </body>
                </html>
                """.formatted(content, java.time.Year.now().getValue());
    }


}
