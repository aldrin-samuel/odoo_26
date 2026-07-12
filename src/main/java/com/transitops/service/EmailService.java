package com.transitops.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:transitops.hackathon@gmail.com}") // Added a fallback just in case
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSuspensionEmail(String toEmail, String driverName) {
        String subject = "URGENT: Your TransitOps Account has been Suspended";
        String body = "Dear " + driverName + ",\n\n" +
                "Your driver account has been suspended because your license has expired or due to a compliance issue.\n\n" +
                "You cannot be assigned to any trips while suspended.\n" +
                "Please contact the Safety Officer or Fleet Manager to resolve this.\n\n" +
                "Regards,\nTransitOps System";

        sendEmail(toEmail, subject, body);
    }

    public void sendLicenseExpiryEmail(String toEmail, String driverName, String expiryDate) {
        String subject = "ACTION REQUIRED: Driver License Expiring Soon";
        String body = "Dear " + driverName + ",\n\n" +
                "This is an automated reminder that your driving license is expiring soon.\n" +
                "Expiry Date: " + expiryDate + "\n\n" +
                "Please renew your license before this date to avoid account suspension.\n\n" +
                "Regards,\nTransitOps System";

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            System.out.println("Attempting to send email...");
            System.out.println("From: " + fromEmail);
            System.out.println("To: " + to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + to);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}