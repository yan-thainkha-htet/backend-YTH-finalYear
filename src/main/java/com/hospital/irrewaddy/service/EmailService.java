package com.hospital.irrewaddy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from:noreply@irrewaddyhospital.com}")
    private String fromEmail;

    @Value("${app.email.from-name:IRREWADDY Hospital}")
    private String fromName;

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set from with proper encoding handling
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                // Fallback: set from without personal name
                helper.setFrom(fromEmail);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML email: " + e.getMessage());
        }
    }

    public void sendOTPEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Email Verification - IRREWADDY Hospital");

            String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #9333ea;">Email Verification</h2>
                    <p>Your OTP code for email verification is:</p>
                    <div style="background-color: #f3e8ff; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; border-radius: 5px; margin: 20px 0;">
                        %s
                    </div>
                    <p>This OTP will expire in 10 minutes.</p>
                    <p>If you didn't request this verification, please ignore this email.</p>
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
                    <p style="font-size: 12px; color: #666;">IRREWADDY Hospital Team</p>
                </div>
            </body>
            </html>
            """, otpCode);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    public void sendPasswordResetOtp(String to, String fullName, String otp, int expiryMinutes) {
        String subject = "Password Reset OTP - IRREWADDY Hospital";

        String htmlContent = buildPasswordResetOtpEmail(fullName, otp, expiryMinutes);

        try {
            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            // Fallback to simple email if HTML fails
            String simpleText = String.format(
                    "Hello %s,\n\n" +
                            "Your password reset OTP is: %s\n\n" +
                            "This OTP is valid for %d minutes.\n\n" +
                            "If you didn't request this, please ignore this email.\n\n" +
                            "Best regards,\nIRREWADDY Hospital Team",
                    fullName, otp, expiryMinutes
            );
            sendSimpleEmail(to, subject, simpleText);
        }
    }

    public void sendPasswordResetConfirmation(String to, String fullName) {
        String subject = "Password Reset Successful - IRREWADDY Hospital";

        String htmlContent = buildPasswordResetConfirmationEmail(fullName);

        try {
            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            // Fallback to simple email
            String simpleText = String.format(
                    "Hello %s,\n\n" +
                            "Your password has been successfully reset.\n\n" +
                            "If you did not perform this action, please contact support immediately.\n\n" +
                            "Best regards,\nIRREWADDY Hospital Team",
                    fullName
            );
            sendSimpleEmail(to, subject, simpleText);
        }
    }

    private String buildPasswordResetOtpEmail(String fullName, String otp, int expiryMinutes) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 20px auto; 
                        background: white;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 0 10px rgba(0,0,0,0.1);
                    }
                    .header { 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                        color: white; 
                        padding: 30px; 
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                    }
                    .header p {
                        margin: 10px 0 0 0;
                        font-size: 16px;
                    }
                    .content { 
                        padding: 30px;
                    }
                    .content h2 {
                        color: #333;
                        margin-top: 0;
                    }
                    .otp-box { 
                        background: #f9f9f9;
                        border: 2px dashed #667eea; 
                        padding: 20px; 
                        text-align: center; 
                        margin: 20px 0; 
                        border-radius: 10px;
                    }
                    .otp-code { 
                        font-size: 32px; 
                        font-weight: bold; 
                        color: #667eea; 
                        letter-spacing: 8px; 
                        margin: 10px 0;
                        font-family: monospace;
                    }
                    .warning { 
                        background: #fff3cd; 
                        border-left: 4px solid #ffc107; 
                        padding: 15px; 
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .warning strong {
                        color: #856404;
                    }
                    .warning ul {
                        margin: 10px 0;
                        padding-left: 20px;
                    }
                    .footer { 
                        text-align: center; 
                        padding: 20px; 
                        background: #f9f9f9;
                        color: #666; 
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏥 IRREWADDY Hospital</h1>
                        <p>Password Reset Request</p>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>We received a request to reset your password. Use the OTP code below to complete the process:</p>
                        
                        <div class="otp-box">
                            <p style="margin: 0; color: #666; font-size: 14px;">Your OTP Code</p>
                            <div class="otp-code">%s</div>
                            <p style="margin: 0; color: #666; font-size: 14px;">Valid for %d minutes</p>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Security Notice:</strong>
                            <ul style="margin: 10px 0;">
                                <li>Never share this OTP with anyone</li>
                                <li>IRREWADDY Hospital staff will never ask for your OTP</li>
                                <li>If you didn't request this, please ignore this email</li>
                            </ul>
                        </div>
                        
                        <p>If you're having trouble, please contact our support team.</p>
                        
                        <p>Best regards,<br><strong>IRREWADDY Hospital Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                        <p>&copy; 2025 IRREWADDY Hospital. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, otp, expiryMinutes);
    }

    private String buildPasswordResetConfirmationEmail(String fullName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 20px auto; 
                        background: white;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 0 10px rgba(0,0,0,0.1);
                    }
                    .header { 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                        color: white; 
                        padding: 30px; 
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                    }
                    .header p {
                        margin: 10px 0 0 0;
                        font-size: 16px;
                    }
                    .content { 
                        padding: 30px;
                    }
                    .content h2 {
                        color: #333;
                        margin-top: 0;
                    }
                    .success-box { 
                        background: #d4edda; 
                        border-left: 4px solid #28a745; 
                        padding: 15px; 
                        margin: 20px 0; 
                        border-radius: 5px;
                    }
                    .success-box strong {
                        color: #155724;
                    }
                    .success-box p {
                        margin: 10px 0 0 0;
                        color: #155724;
                    }
                    .footer { 
                        text-align: center; 
                        padding: 20px; 
                        background: #f9f9f9;
                        color: #666; 
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏥 IRREWADDY Hospital</h1>
                        <p>Password Reset Confirmation</p>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        
                        <div class="success-box">
                            <strong>✅ Success!</strong>
                            <p>Your password has been successfully reset.</p>
                        </div>
                        
                        <p>You can now login to your account using your new password.</p>
                        
                        <p><strong>If you did not perform this action:</strong></p>
                        <ul>
                            <li>Contact our support team immediately</li>
                            <li>Change your password as soon as possible</li>
                            <li>Review your recent account activity</li>
                        </ul>
                        
                        <p>Best regards,<br><strong>IRREWADDY Hospital Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                        <p>&copy; 2025 IRREWADDY Hospital. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName);
    }

//    public void sendAdminWelcomeEmail(String to, String username, String tempPassword) {
//        String subject = "Welcome to IRREWADDY Hospital - Admin Account Created";
//
//        String htmlContent = """
//            <!DOCTYPE html>
//            <html>
//            <head>
//                <meta charset="UTF-8">
//                <style>
//                    body {
//                        font-family: Arial, sans-serif;
//                        line-height: 1.6;
//                        color: #333;
//                        margin: 0;
//                        padding: 0;
//                        background-color: #f4f4f4;
//                    }
//                    .container {
//                        max-width: 600px;
//                        margin: 20px auto;
//                        background: white;
//                        border-radius: 10px;
//                        overflow: hidden;
//                        box-shadow: 0 0 10px rgba(0,0,0,0.1);
//                    }
//                    .header {
//                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
//                        color: white;
//                        padding: 30px;
//                        text-align: center;
//                    }
//                    .content {
//                        padding: 30px;
//                    }
//                    .credentials-box {
//                        background: #f8f9fa;
//                        border-left: 4px solid #667eea;
//                        padding: 20px;
//                        margin: 20px 0;
//                        border-radius: 5px;
//                    }
//                    .credential-item {
//                        margin: 10px 0;
//                        padding: 10px;
//                        background: white;
//                        border-radius: 5px;
//                    }
//                    .credential-label {
//                        font-weight: bold;
//                        color: #666;
//                        font-size: 12px;
//                        text-transform: uppercase;
//                    }
//                    .credential-value {
//                        font-size: 16px;
//                        color: #333;
//                        font-family: monospace;
//                        margin-top: 5px;
//                    }
//                    .warning {
//                        background: #fff3cd;
//                        border-left: 4px solid #ffc107;
//                        padding: 15px;
//                        margin: 20px 0;
//                        border-radius: 5px;
//                    }
//                    .button {
//                        display: inline-block;
//                        padding: 12px 30px;
//                        background: #667eea;
//                        color: white;
//                        text-decoration: none;
//                        border-radius: 5px;
//                        margin: 20px 0;
//                    }
//                    .footer {
//                        text-align: center;
//                        padding: 20px;
//                        background: #f9f9f9;
//                        color: #666;
//                        font-size: 12px;
//                    }
//                    ul {
//                        padding-left: 20px;
//                    }
//                </style>
//            </head>
//            <body>
//                <div class="container">
//                    <div class="header">
//                        <h1>🏥 IRREWADDY Hospital</h1>
//                        <p>Admin Account Created</p>
//                    </div>
//                    <div class="content">
//                        <h2>Welcome, %s!</h2>
//                        <p>Your administrator account has been successfully created at IRREWADDY Hospital Management System.</p>
//
//                        <div class="credentials-box">
//                            <h3 style="margin-top: 0; color: #667eea;">Your Login Credentials</h3>
//
//                            <div class="credential-item">
//                                <div class="credential-label">Username</div>
//                                <div class="credential-value">%s</div>
//                            </div>
//
//                            <div class="credential-item">
//                                <div class="credential-label">Email</div>
//                                <div class="credential-value">%s</div>
//                            </div>
//
//                            <div class="credential-item">
//                                <div class="credential-label">Temporary Password</div>
//                                <div class="credential-value">%s</div>
//                            </div>
//                        </div>
//
//                        <div class="warning">
//                            <strong>⚠️ Important Security Notice:</strong>
//                            <ul style="margin: 10px 0;">
//                                <li><strong>You must change your password</strong> on first login</li>
//                                <li>Do not share your credentials with anyone</li>
//                                <li>Use a strong, unique password</li>
//                                <li>Keep your login information secure</li>
//                            </ul>
//                        </div>
//
//                        <p><strong>Your Responsibilities:</strong></p>
//                        <ul>
//                            <li>Manage hospital staff and departments</li>
//                            <li>Oversee system operations</li>
//                            <li>Handle user accounts and permissions</li>
//                            <li>Ensure system security and compliance</li>
//                        </ul>
//
//                        <p>If you have any questions or need assistance, please contact the IT department.</p>
//
//                        <p>Best regards,<br><strong>IRREWADDY Hospital Team</strong></p>
//                    </div>
//                    <div class="footer">
//                        <p>This is an automated email. Please do not reply.</p>
//                        <p>&copy; 2025 IRREWADDY Hospital. All rights reserved.</p>
//                    </div>
//                </div>
//            </body>
//            </html>
//            """.formatted(username, to, tempPassword);
//
//        try {
//            sendHtmlEmail(to, subject, htmlContent);
//            System.out.println("✅ Welcome email sent to admin: " + to);
//        } catch (Exception e) {
//            System.err.println("❌ Failed to send welcome email to admin: " + e.getMessage());
//            // Fallback to simple email
//            String simpleText = String.format(
//                    "Welcome to IRREWADDY Hospital, %s!\n\n" +
//                            "Your admin account has been created.\n\n" +
//                            "Username: %s\n" +
//                            "Email: %s\n" +
//                            "Temporary Password: %s\n\n" +
//                            "IMPORTANT: You must change your password on first login.\n\n" +
//                            "Best regards,\nIRREWADDY Hospital Team",
//                    username, to, tempPassword
//            );
//            sendSimpleEmail(to, subject, simpleText);
//        }
//    }

    public void sendAdminWelcomeEmail(String to, String username, String tempPassword) {
        String subject = "Welcome to IRREWADDY Hospital - Admin Account Created";

        String htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Welcome Email</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f4f4f4;
                    margin: 0;
                    padding: 0;
                }
                .email-container {
                    background-color: #ffffff;
                    width: 90%%;
                    max-width: 600px;
                    margin: 40px auto;
                    padding: 30px;
                    border-radius: 10px;
                    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                }
                .header {
                    text-align: center;
                    color: #0a5e9a;
                }
                .content {
                    margin-top: 20px;
                    font-size: 16px;
                    color: #333;
                    line-height: 1.6;
                }
                .credentials {
                    background: #f1f9ff;
                    padding: 15px;
                    border-radius: 8px;
                    margin-top: 20px;
                }
                .credential-label {
                    font-weight: bold;
                }
                .footer {
                    text-align: center;
                    margin-top: 30px;
                    font-size: 14px;
                    color: #888;
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <h2>Welcome, %s!</h2>
                    <p>Your admin account has been created successfully.</p>
                </div>
                <div class="content">
                    <p>Hello <b>%s</b>,</p>
                    <p>Welcome to <b>IRREWADDY Hospital</b>. Your admin account has been successfully created.</p>
                    <p>Here are your login credentials:</p>

                    <div class="credentials">
                        <p><span class="credential-label">Username:</span> %s</p>
                        <p><span class="credential-label">Email:</span> %s</p>
                        <p><span class="credential-label">Temporary Password:</span> %s</p>
                    </div>

                    <p><b>Important:</b> You must change your password upon first login for security reasons.</p>
                </div>
                <div class="footer">
                    <p>© 2025 IRREWADDY Hospital. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(username, username, username, to, tempPassword); // ✅ fixed argument count

        try {
            sendHtmlEmail(to, subject, htmlContent);
            System.out.println("✅ Welcome email sent to admin: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send welcome email to admin: " + e.getMessage());
            String simpleText = String.format(
                    "Welcome to IRREWADDY Hospital, %s!\n\n" +
                            "Your admin account has been created.\n\n" +
                            "Username: %s\n" +
                            "Email: %s\n" +
                            "Temporary Password: %s\n\n" +
                            "IMPORTANT: You must change your password on first login.\n\n" +
                            "Best regards,\nIRREWADDY Hospital Team",
                    username, username, to, tempPassword
            );
            sendSimpleEmail(to, subject, simpleText);
        }
    }


    public void sendDoctorWelcomeEmail(String to, String fullName, String username, String tempPassword,
                                       String specialization, String department) {
        String subject = "Welcome to IRREWADDY Hospital - Doctor Account Created";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 20px auto; 
                        background: white;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 0 10px rgba(0,0,0,0.1);
                    }
                    .header { 
                        background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); 
                        color: white; 
                        padding: 30px; 
                        text-align: center;
                    }
                    .content { 
                        padding: 30px;
                    }
                    .credentials-box { 
                        background: #f8f9fa;
                        border-left: 4px solid #11998e; 
                        padding: 20px; 
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .credential-item {
                        margin: 10px 0;
                        padding: 10px;
                        background: white;
                        border-radius: 5px;
                    }
                    .credential-label {
                        font-weight: bold;
                        color: #666;
                        font-size: 12px;
                        text-transform: uppercase;
                    }
                    .credential-value {
                        font-size: 16px;
                        color: #333;
                        font-family: monospace;
                        margin-top: 5px;
                    }
                    .info-box {
                        background: #e7f3ff;
                        border-left: 4px solid #2196F3;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .warning { 
                        background: #fff3cd; 
                        border-left: 4px solid #ffc107; 
                        padding: 15px; 
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .footer { 
                        text-align: center; 
                        padding: 20px; 
                        background: #f9f9f9;
                        color: #666; 
                        font-size: 12px;
                    }
                    ul {
                        padding-left: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏥 IRREWADDY Hospital</h1>
                        <p>Doctor Account Created</p>
                    </div>
                    <div class="content">
                        <h2>Welcome, Dr. %s!</h2>
                        <p>Your doctor account has been successfully created at IRREWADDY Hospital Management System.</p>
                        
                        <div class="info-box">
                            <strong>📋 Your Profile Information:</strong>
                            <p style="margin: 10px 0 5px 0;"><strong>Specialization:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Department:</strong> %s</p>
                        </div>
                        
                        <div class="credentials-box">
                            <h3 style="margin-top: 0; color: #11998e;">Your Login Credentials</h3>
                            
                            <div class="credential-item">
                                <div class="credential-label">Username</div>
                                <div class="credential-value">%s</div>
                            </div>
                            
                            <div class="credential-item">
                                <div class="credential-label">Email</div>
                                <div class="credential-value">%s</div>
                            </div>
                            
                            <div class="credential-item">
                                <div class="credential-label">Temporary Password</div>
                                <div class="credential-value">%s</div>
                            </div>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Important Security Notice:</strong>
                            <ul style="margin: 10px 0;">
                                <li><strong>You must change your password</strong> on first login</li>
                                <li>Do not share your credentials with anyone</li>
                                <li>Use a strong, unique password</li>
                                <li>Keep your login information secure</li>
                            </ul>
                        </div>
                        
                        <p><strong>System Features You Can Access:</strong></p>
                        <ul>
                            <li>View and manage your appointments</li>
                            <li>Access patient medical records</li>
                            <li>Update appointment status</li>
                            <li>Add medical notes and prescriptions</li>
                        </ul>
                        
                        <p>If you have any questions or need assistance, please contact the administration.</p>
                        
                        <p>Best regards,<br><strong>IRREWADDY Hospital Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                        <p>&copy; 2025 IRREWADDY Hospital. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, specialization, department, username, to, tempPassword);

        try {
            sendHtmlEmail(to, subject, htmlContent);
            System.out.println("✅ Welcome email sent to doctor: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send welcome email to doctor: " + e.getMessage());
            // Fallback to simple email
            String simpleText = String.format(
                    "Welcome to IRREWADDY Hospital, Dr. %s!\n\n" +
                            "Your doctor account has been created.\n\n" +
                            "Specialization: %s\n" +
                            "Department: %s\n\n" +
                            "Username: %s\n" +
                            "Email: %s\n" +
                            "Temporary Password: %s\n\n" +
                            "IMPORTANT: You must change your password on first login.\n\n" +
                            "Best regards,\nIRREWADDY Hospital Team",
                    fullName, specialization, department, username, to, tempPassword
            );
            sendSimpleEmail(to, subject, simpleText);
        }
    }

    /**
     * Send welcome email to newly created receptionist
     */
    public void sendReceptionistWelcomeEmail(String to, String fullName, String username, String tempPassword, String shift, Integer deskNumber) {
        String subject = "Welcome to IRREWADDY Hospital - Receptionist Account Created";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 20px auto; 
                        background: white;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 0 10px rgba(0,0,0,0.1);
                    }
                    .header { 
                        background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); 
                        color: white; 
                        padding: 30px; 
                        text-align: center;
                    }
                    .content { 
                        padding: 30px;
                    }
                    .credentials-box { 
                        background: #f8f9fa;
                        border-left: 4px solid #f093fb; 
                        padding: 20px; 
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .credential-item {
                        margin: 10px 0;
                        padding: 10px;
                        background: white;
                        border-radius: 5px;
                    }
                    .credential-label {
                        font-weight: bold;
                        color: #666;
                        font-size: 12px;
                        text-transform: uppercase;
                    }
                    .credential-value {
                        font-size: 16px;
                        color: #333;
                        font-family: monospace;
                        margin-top: 5px;
                    }
                    .info-box {
                        background: #fff4e6;
                        border-left: 4px solid #ff9800;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .warning { 
                        background: #fff3cd; 
                        border-left: 4px solid #ffc107; 
                        padding: 15px; 
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .footer { 
                        text-align: center; 
                        padding: 20px; 
                        background: #f9f9f9;
                        color: #666; 
                        font-size: 12px;
                    }
                    ul {
                        padding-left: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏥 IRREWADDY Hospital</h1>
                        <p>Receptionist Account Created</p>
                    </div>
                    <div class="content">
                        <h2>Welcome, %s!</h2>
                        <p>Your receptionist account has been successfully created at IRREWADDY Hospital Management System.</p>
                        
                        <div class="info-box">
                            <strong>📋 Your Work Information:</strong>
                            <p style="margin: 10px 0 5px 0;"><strong>Employee ID:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Shift:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Desk Number:</strong> %s</p>
                        </div>
                        
                        <div class="credentials-box">
                            <h3 style="margin-top: 0; color: #f093fb;">Your Login Credentials</h3>
                            
                            <div class="credential-item">
                                <div class="credential-label">Username</div>
                                <div class="credential-value">%s</div>
                            </div>
                            
                            <div class="credential-item">
                                <div class="credential-label">Email</div>
                                <div class="credential-value">%s</div>
                            </div>
                            
                            <div class="credential-item">
                                <div class="credential-label">Temporary Password</div>
                                <div class="credential-value">%s</div>
                            </div>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Important Security Notice:</strong>
                            <ul style="margin: 10px 0;">
                                <li><strong>You must change your password</strong> on first login</li>
                                <li>Do not share your credentials with anyone</li>
                                <li>Use a strong, unique password</li>
                                <li>Keep your login information secure</li>
                            </ul>
                        </div>
                        
                        <p><strong>Your Responsibilities:</strong></p>
                        <ul>
                            <li>Manage patient appointments</li>
                            <li>Register new patients</li>
                            <li>Handle patient check-ins</li>
                            <li>Coordinate with doctors and patients</li>
                            <li>Maintain front desk operations</li>
                        </ul>
                        
                        <p>If you have any questions or need assistance, please contact your supervisor.</p>
                        
                        <p>Best regards,<br><strong>IRREWADDY Hospital Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                        <p>&copy; 2025 IRREWADDY Hospital. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, shift,
                deskNumber != null ? "Desk " + deskNumber : "Not assigned",
                username, to, tempPassword);

        try {
            sendHtmlEmail(to, subject, htmlContent);
            System.out.println("✅ Welcome email sent to receptionist: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send welcome email to receptionist: " + e.getMessage());
            // Fallback to simple email
            String simpleText = String.format(
                    "Welcome to IRREWADDY Hospital, %s!\n\n" +
                            "Your receptionist account has been created.\n\n" +
                            "Employee ID: %s\n" +
                            "Shift: %s\n" +
                            "Desk Number: %s\n\n" +
                            "Username: %s\n" +
                            "Email: %s\n" +
                            "Temporary Password: %s\n\n" +
                            "IMPORTANT: You must change your password on first login.\n\n" +
                            "Best regards,\nIRREWADDY Hospital Team",
                    fullName, shift,
                    deskNumber != null ? "Desk " + deskNumber : "Not assigned",
                    username, to, tempPassword
            );
            sendSimpleEmail(to, subject, simpleText);
        }
    }
}