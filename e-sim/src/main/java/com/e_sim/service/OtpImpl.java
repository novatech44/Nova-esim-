package com.e_sim.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.e_sim.dao.entity.OtpVerification;
import com.e_sim.dao.repository.OtpVerificationRepository;
import com.e_sim.dto.request.EmailDetails;
import com.e_sim.dto.request.OtpReq;
import com.e_sim.exception.OtpExpiredException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Slf4j
public class OtpImpl{

    private final OtpVerificationRepository otpVerificationRepository;
    private static final int OTP_EXPIRY_MINUTES = 20;
    private final Emailservice emailservice;

   
    public String generateOtp() {
        try {
            SecureRandom secureRandom = new SecureRandom();
            int otpValue = 100000 + secureRandom.nextInt(900000); 
            return String.valueOf(otpValue);
        } catch (Exception e) {
            log.info("Error generating OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating OTP", e);
        }
    }

   
    @Transactional(propagation = Propagation.REQUIRED)
    public void sendOtp(OtpReq req) {
    // public void sendOtp(OtpReq req) {
        String email = req.getEmail();
        log.info("email where otp is being sent to: {}", email);  
        // String email = req.getEmail().trim().toLowerCase();  // Normalize the email

        try {
            // Clean up expired OTPs before sending a new one
            otpVerificationRepository.deleteAllByEmailAndVerifiedFalse(email);

            String otp = generateOtp();
            OtpVerification otpEntity = new OtpVerification();
            otpEntity.setEmail(email);
            otpEntity.setOtpCode(otp);
            otpEntity.setCreatedAt(LocalDateTime.now());
            otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));

            OtpVerification saved = otpVerificationRepository.save(otpEntity);
            log.info("Saved OTP [{}] for email: {}", saved.getOtpCode(), saved.getEmail());
            sendOtpEmail(email, otp);
            // return otp;
        } catch (Exception e) {
            log.info("Error sending OTP for email: {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error sending OTP", e);
        }
    }

   
    @Transactional
    public String verifyOtp(OtpReq request) {

        String otpCode = request.getOtp().trim();
        log.info("Looking for OTP in storage with code: {}", otpCode);

        try {
            // compare OTP from repository and ensure it's valid
            OtpVerification otpVerification = findOtpVerification(otpCode);
            log.info("Found OTP: {} for email: {}", otpVerification.getOtpCode(), otpVerification.getEmail());

            // Check if the OTP has expired
            validateOtpExpiration(otpVerification);

            // Mark OTP as verified
            otpVerification.setVerified(true);
            otpVerificationRepository.save(otpVerification);

            String email = otpVerification.getEmail();
            log.info("OTP verified for email: {}", email);

            // Send verified email success message
            sendVerificationSuccessEmail(email);

            log.info("OTP verified successfully for email: {}", email);
            return email;
        } catch (Exception e) {
            log.info("Error verifying OTP for code: {}: {}", otpCode, e.getMessage(), e);
            throw new RuntimeException("Error verifying OTP", e);
        }
    }

    private OtpVerification findOtpVerification(String otpCode) {
        try {
            OtpVerification otpVerification = otpVerificationRepository
                .findFirstByOtpCodeAndVerifiedFalseOrderByExpiresAtAsc(otpCode)
                .orElseThrow(() -> {
                    log.info("Invalid OTP: {}", otpCode);
                    return new RuntimeException("Invalid OTP");
                });

            return otpVerification;
        } catch (Exception e) {
            log.info("Error fetching OTP verification for code: {}: {}", otpCode, e.getMessage(), e);
            throw new RuntimeException("Error fetching OTP verification", e);
        }
    }

    private void validateOtpExpiration(OtpVerification otpVerification) {
        try {
            if (otpVerification.getExpiresAt() == null || otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.info("OTP expired for email: {}", otpVerification.getEmail());
                throw new OtpExpiredException("OTP has expired");
            }
        } catch (Exception e) {
            log.info("Error validating OTP expiration: {}", e.getMessage(), e);
            throw new RuntimeException("Error validating OTP expiration", e);
        }
    }

    // private void sendVerificationSuccessEmail(String email) {
    //     try {
    //         EmailDetails emailDetails = EmailDetails.builder()
    //             .reciepinet(email)
    //             .subject("Miala OTP Verification: ")
    //             .messageBody("""
    //                 Your OTP has been verified successfully. 
    //                 Wait while admin reviews your information...
    //                 """)
    //             .build();

    //         emailservice.sendEmail(emailDetails);
    //         log.info("Verification success email sent to: {}", email);
    //     } catch (Exception e) {
    //         log.info("Error sending verification success email to: {}: {}", email, e.getMessage(), e);
    //         throw new RuntimeException("Error sending verification success email", e);
    //     }
    // }
    private void sendVerificationSuccessEmail(String email) {
        try {
            String htmlBody = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;">
                <div style="max-width: 500px; margin: auto; background: #ffffff; border-radius: 8px; padding: 20px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <h2 style="color: #fa0d0dff;">Miala Verification Successful</h2>
                    <p>Hello,</p>
                    <p>Your OTP has been verified successfully.</p>
                    <p>Please wait while the admin reviews your information.</p>
                    <hr>
                    <small style="color: #888;">If this was not you, please contact our support team immediately.</small>
                </div>
                </body>
                </html>
            """;

            EmailDetails emailDetails = EmailDetails.builder()
                .reciepinet(email)
                .subject("Miala OTP Verification Successful")
                .messageBody(htmlBody)
                .build();

            emailservice.sendEmail(emailDetails);
            log.info("Verification success email sent to: {}", email);
        } catch (Exception e) {
            log.info("Error sending verification success email to: {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error sending verification success email", e);
        }
    }


    // private void sendOtpEmail(String email, String otp) {
    //     try {
    //         EmailDetails emailDetails = EmailDetails.builder()
    //             .subject("Miala OTP")
    //             .reciepinet(email)
    //             .messageBody("Your OTP code is: " + otp + ". It expires in " + OTP_EXPIRY_MINUTES + " minutes.")
    //             .build();

    //         emailservice.sendEmail(emailDetails);
    //         log.info("OTP sent to email: {}", email);
    //     } catch (Exception e) {
    //         log.info("Error sending OTP email to: {}: {}", email, e.getMessage(), e);
    //         throw new RuntimeException("Error sending OTP email", e);
    //     }
    // }
    private void sendOtpEmail(String email, String otp) {
        try {
            String htmlBody =
                "<html>\n" +
                "<body style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;\">\n" +
                "<div style=\"max-width: 500px; margin: auto; background: #ffffff; border-radius: 8px; padding: 20px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">\n" +
                "    <h2 style=\"color: #fa0d0dff;\">OTP Verification</h2>\n" +
                "    <p>Hello,</p>\n" +
                "    <p>Your one-time password is:</p>\n" +
                "    <h1 style=\"text-align: center; color: #fa0d0dff;\">" + otp + "</h1>\n" +
                "    <p>This code will expire in <strong>" + OTP_EXPIRY_MINUTES + " minutes</strong>. Please do not share it with anyone.</p>\n" +
                "    <hr>\n" +
                "    <small style=\"color: #888;\">If you did not request this OTP, please ignore this message.</small>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";

            EmailDetails emailDetails = EmailDetails.builder()
                .subject("Miala OTP")
                .reciepinet(email)
                .messageBody(htmlBody)
                .build();

            emailservice.sendEmail(emailDetails);
            log.info("OTP sent to email: {}", email);
        } catch (Exception e) {
            log.info("Error sending OTP email to: {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error sending OTP email", e);
        }
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public int cleanupExpiredOtp() {
        try {
            LocalDateTime now = LocalDateTime.now();

            boolean hasExpired = otpVerificationRepository.existsByExpiresAtBefore(now);
            if (!hasExpired) {
                log.info("No expired OTP records to delete.");
                return 0;
            }
            int deletedCount = otpVerificationRepository.deleteAllByExpiresAtBefore(now);

            log.info("Deleted {} expired OTP records.", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.info("Error cleaning up expired OTPs: {}", e.getMessage(), e);
            throw new RuntimeException("Error cleaning up expired OTPs", e);
        }
    }

    @Transactional
    @Scheduled(fixedRate = 300000) 
    public void scheduleExpiredOtpCleanup() {
        try {
            cleanupExpiredOtp();
        } catch (Exception e) {
            log.info("Error scheduling expired OTP cleanup: {}", e.getMessage(), e);
        }
    }

}

