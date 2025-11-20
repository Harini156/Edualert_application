<?php
/**
 * PHPMailer Setup Test Script
 * Use this to verify your PHPMailer installation and Gmail configuration
 * 
 * HOW TO USE:
 * 1. Upload all PHPMailer files to /api/ folder
 * 2. Add your Gmail App Password to this file (line 25)
 * 3. Add your test email address (line 26)
 * 4. Visit: http://your-domain.com/api/test_phpmailer_setup.php
 * 5. Check if test email is received
 */

header('Content-Type: text/html; charset=UTF-8');

// Load PHPMailer
require_once 'PHPMailer.php';
require_once 'SMTP.php';
require_once 'Exception.php';

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;

// ⚠️ CONFIGURE THESE:
$gmail_app_password = 'qzlthmrgeilchifg';  // Gmail App Password configured
$test_email = 'your_test_email@example.com';           // REPLACE WITH YOUR EMAIL FOR TESTING

?>
<!DOCTYPE html>
<html>
<head>
    <title>PHPMailer Setup Test</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
            background: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #922381;
            border-bottom: 3px solid #922381;
            padding-bottom: 10px;
        }
        .success {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
        }
        .error {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
        }
        .warning {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            color: #856404;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
        }
        .info {
            background: #d1ecf1;
            border: 1px solid #bee5eb;
            color: #0c5460;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
        }
        .check-item {
            padding: 10px;
            margin: 5px 0;
            border-left: 4px solid #ddd;
            padding-left: 15px;
        }
        .check-pass {
            border-left-color: #28a745;
            background: #f0fff4;
        }
        .check-fail {
            border-left-color: #dc3545;
            background: #fff5f5;
        }
        code {
            background: #f4f4f4;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: 'Courier New', monospace;
        }
        .btn {
            background: #922381;
            color: white;
            padding: 12px 30px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            text-decoration: none;
            display: inline-block;
            margin: 10px 0;
        }
        .btn:hover {
            background: #7a1d6b;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 PHPMailer Setup Test</h1>
        
        <?php
        // Check if configuration is done
        if ($gmail_app_password === 'your_gmail_app_password_here' || $test_email === 'your_test_email@example.com') {
            echo '<div class="warning">';
            echo '<h3>⚠️ Configuration Required</h3>';
            echo '<p>Please edit this file and configure:</p>';
            echo '<ol>';
            echo '<li>Line 25: Add your Gmail App Password</li>';
            echo '<li>Line 26: Add your test email address</li>';
            echo '</ol>';
            echo '<p><strong>How to get Gmail App Password:</strong></p>';
            echo '<ol>';
            echo '<li>Go to <a href="https://myaccount.google.com/security" target="_blank">Google Account Security</a></li>';
            echo '<li>Enable 2-Step Verification</li>';
            echo '<li>Click "App passwords"</li>';
            echo '<li>Generate password for Mail → Other (EduAlert)</li>';
            echo '<li>Copy the 16-character password</li>';
            echo '</ol>';
            echo '</div>';
            exit;
        }
        
        echo '<div class="info">';
        echo '<h3>📋 System Check</h3>';
        echo '</div>';
        
        // Check 1: PHPMailer files exist
        echo '<div class="check-item ' . (file_exists('PHPMailer.php') ? 'check-pass' : 'check-fail') . '">';
        echo file_exists('PHPMailer.php') ? '✅' : '❌';
        echo ' PHPMailer.php file exists';
        echo '</div>';
        
        echo '<div class="check-item ' . (file_exists('SMTP.php') ? 'check-pass' : 'check-fail') . '">';
        echo file_exists('SMTP.php') ? '✅' : '❌';
        echo ' SMTP.php file exists';
        echo '</div>';
        
        echo '<div class="check-item ' . (file_exists('Exception.php') ? 'check-pass' : 'check-fail') . '">';
        echo file_exists('Exception.php') ? '✅' : '❌';
        echo ' Exception.php file exists';
        echo '</div>';
        
        // Check 2: PHP version
        $php_version_ok = version_compare(PHP_VERSION, '5.5.0', '>=');
        echo '<div class="check-item ' . ($php_version_ok ? 'check-pass' : 'check-fail') . '">';
        echo $php_version_ok ? '✅' : '❌';
        echo ' PHP Version: ' . PHP_VERSION . ' (5.5.0+ required)';
        echo '</div>';
        
        // Check 3: OpenSSL extension
        $openssl_ok = extension_loaded('openssl');
        echo '<div class="check-item ' . ($openssl_ok ? 'check-pass' : 'check-fail') . '">';
        echo $openssl_ok ? '✅' : '❌';
        echo ' OpenSSL extension (required for TLS)';
        echo '</div>';
        
        // If basic checks fail, stop here
        if (!file_exists('PHPMailer.php') || !file_exists('SMTP.php') || !file_exists('Exception.php')) {
            echo '<div class="error">';
            echo '<h3>❌ Missing Files</h3>';
            echo '<p>Please upload all PHPMailer files to the /api/ folder:</p>';
            echo '<ul>';
            echo '<li>PHPMailer.php</li>';
            echo '<li>SMTP.php</li>';
            echo '<li>Exception.php</li>';
            echo '</ul>';
            echo '</div>';
            exit;
        }
        
        if (!$openssl_ok) {
            echo '<div class="error">';
            echo '<h3>❌ OpenSSL Extension Missing</h3>';
            echo '<p>Contact your hosting provider to enable the PHP OpenSSL extension.</p>';
            echo '</div>';
            exit;
        }
        
        // Try to send test email
        echo '<div class="info">';
        echo '<h3>📧 Sending Test Email...</h3>';
        echo '</div>';
        
        try {
            $mail = new PHPMailer(true);
            
            // Enable verbose debug output
            $mail->SMTPDebug = 0;  // Set to 2 for detailed debugging
            $mail->Debugoutput = 'html';
            
            // SMTP Configuration
            $mail->isSMTP();
            $mail->Host       = 'smtp.gmail.com';
            $mail->SMTPAuth   = true;
            $mail->Username   = 'edualert.notifications@gmail.com';
            $mail->Password   = $gmail_app_password;
            $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
            $mail->Port       = 587;
            $mail->CharSet    = 'UTF-8';
            
            // Recipients
            $mail->setFrom('edualert.notifications@gmail.com', 'EduAlert Test');
            $mail->addAddress($test_email);
            
            // Content
            $mail->isHTML(true);
            $mail->Subject = 'EduAlert PHPMailer Test - ' . date('Y-m-d H:i:s');
            $mail->Body    = '
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                <h2 style="color: #922381;">✅ PHPMailer Test Successful!</h2>
                <p>Congratulations! Your PHPMailer setup is working correctly.</p>
                <div style="background: #f0f0f0; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <h3>Test Details:</h3>
                    <ul>
                        <li><strong>From:</strong> edualert.notifications@gmail.com</li>
                        <li><strong>To:</strong> ' . $test_email . '</li>
                        <li><strong>Time:</strong> ' . date('Y-m-d H:i:s') . '</li>
                        <li><strong>Server:</strong> ' . $_SERVER['SERVER_NAME'] . '</li>
                    </ul>
                </div>
                <p style="color: #28a745; font-weight: bold;">Your forgot password feature is ready to use!</p>
            </div>';
            
            $mail->AltBody = 'PHPMailer Test Successful! Your setup is working correctly.';
            
            $mail->send();
            
            echo '<div class="success">';
            echo '<h3>✅ Test Email Sent Successfully!</h3>';
            echo '<p><strong>Check your inbox:</strong> ' . htmlspecialchars($test_email) . '</p>';
            echo '<p>If you don\'t see the email:</p>';
            echo '<ul>';
            echo '<li>Check your spam/junk folder</li>';
            echo '<li>Wait 1-2 minutes (Gmail may delay first email)</li>';
            echo '<li>Verify the email address is correct</li>';
            echo '</ul>';
            echo '<h4>🎉 Your PHPMailer setup is complete!</h4>';
            echo '<p>You can now use the forgot password feature in your app.</p>';
            echo '</div>';
            
            echo '<div class="info">';
            echo '<h3>📝 Next Steps:</h3>';
            echo '<ol>';
            echo '<li>Update <code>send_otp.php</code> with your Gmail App Password</li>';
            echo '<li>Update <code>reset_password.php</code> with your Gmail App Password</li>';
            echo '<li>Test the forgot password flow in your Android app</li>';
            echo '<li>Delete this test file for security</li>';
            echo '</ol>';
            echo '</div>';
            
        } catch (Exception $e) {
            echo '<div class="error">';
            echo '<h3>❌ Email Send Failed</h3>';
            echo '<p><strong>Error:</strong> ' . htmlspecialchars($mail->ErrorInfo) . '</p>';
            echo '<h4>Common Solutions:</h4>';
            echo '<ul>';
            echo '<li><strong>Authentication failed:</strong> Check your Gmail App Password is correct</li>';
            echo '<li><strong>SMTP connect failed:</strong> Check your server can connect to smtp.gmail.com:587</li>';
            echo '<li><strong>Invalid address:</strong> Verify email addresses are correct</li>';
            echo '</ul>';
            echo '<h4>Troubleshooting Steps:</h4>';
            echo '<ol>';
            echo '<li>Verify 2-Step Verification is enabled on Gmail</li>';
            echo '<li>Generate a new App Password</li>';
            echo '<li>Check the app password has no spaces when pasted</li>';
            echo '<li>Contact your hosting provider if connection issues persist</li>';
            echo '</ol>';
            echo '</div>';
        }
        ?>
        
        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;">
            <p><strong>Need help?</strong> Check the <code>PHPMAILER_DEPLOYMENT_GUIDE.md</code> file for detailed instructions.</p>
        </div>
    </div>
</body>
</html>
