<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

include 'db.php';

$response = [];

try {
    if ($_SERVER["REQUEST_METHOD"] == "POST") {
        $email = trim($_POST['email'] ?? '');

        // Validate input
        if (empty($email)) {
            $response['status'] = 'error';
            $response['message'] = 'Email is required.';
            echo json_encode($response);
            exit;
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $response['status'] = 'error';
            $response['message'] = 'Invalid email format.';
            echo json_encode($response);
            exit;
        }

        // Check if user exists
        $userCheckStmt = $conn->prepare("SELECT user_id, name FROM users WHERE email = ?");
        $userCheckStmt->bind_param("s", $email);
        $userCheckStmt->execute();
        $userResult = $userCheckStmt->get_result();

        if ($userResult->num_rows === 0) {
            $response['status'] = 'error';
            $response['message'] = 'No account found with this email address.';
            echo json_encode($response);
            exit;
        }

        $user = $userResult->fetch_assoc();
        $userName = $user['name'];
        $userCheckStmt->close();

        // Generate 6-digit OTP
        $otp = sprintf("%06d", mt_rand(100000, 999999));
        
        // Set expiration time (10 minutes from now)
        $expiresAt = date('Y-m-d H:i:s', strtotime('+10 minutes'));

        // Get user ID for the existing password_reset table
        $userId = $user['user_id'];

        // Clean up old OTPs for this user (optional - keep database clean)
        $cleanupStmt = $conn->prepare("DELETE FROM password_reset WHERE user_id = ? AND expiry < NOW()");
        $cleanupStmt->bind_param("s", $userId);
        $cleanupStmt->execute();
        $cleanupStmt->close();

        // Insert new OTP using existing table structure
        $insertStmt = $conn->prepare("INSERT INTO password_reset (user_id, otp, expiry) VALUES (?, ?, ?)");
        $insertStmt->bind_param("sss", $userId, $otp, $expiresAt);

        if ($insertStmt->execute()) {
            // Send OTP via email
            $emailSent = sendOtpEmail($email, $userName, $otp);
            
            if ($emailSent) {
                $response['status'] = 'success';
                $response['message'] = 'OTP sent successfully to your email address.';
            } else {
                $response['status'] = 'error';
                $response['message'] = 'OTP generated but failed to send email. Please try again.';
            }
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Failed to generate OTP. Please try again.';
        }

        $insertStmt->close();
    } else {
        $response['status'] = 'error';
        $response['message'] = 'Invalid request method.';
    }
} catch (Exception $e) {
    $response['status'] = 'error';
    $response['message'] = 'Server error occurred. Please try again later.';
    error_log("Send OTP Error: " . $e->getMessage());
}

if (isset($conn)) {
    $conn->close();
}

echo json_encode($response);

/**
 * Send OTP email using PHPMailer with Gmail SMTP
 * Production-ready email solution
 */
function sendOtpEmail($email, $userName, $otp) {
    // Gmail SMTP Configuration
    $smtp_host = 'smtp.gmail.com';
    $smtp_port = 587;
    $smtp_username = 'edualert.notifications@gmail.com';
    $smtp_password = 'qzlt hmrg eilc hifg'; // App password
    $from_email = 'edualert.notifications@gmail.com';
    $from_name = 'EduAlert';
    
    // Email content
    $subject = "EduAlert - Password Reset OTP";
    $html_message = "
    <html>
    <head>
        <title>Password Reset OTP</title>
    </head>
    <body>
        <div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>
            <div style='text-align: center; margin-bottom: 30px;'>
                <h2 style='color: #922381; margin: 0;'>🔐 EduAlert</h2>
                <p style='color: #666; margin: 5px 0;'>Password Reset Request</p>
            </div>
            
            <p style='font-size: 16px;'>Dear <strong>$userName</strong>,</p>
            
            <p style='font-size: 14px; line-height: 1.6;'>
                You have requested to reset your password for your EduAlert account. 
                Please use the following One-Time Password (OTP) to proceed:
            </p>
            
            <div style='background: linear-gradient(135deg, #922381, #b8336a); padding: 20px; text-align: center; border-radius: 8px; margin: 25px 0;'>
                <div style='background: white; padding: 15px; border-radius: 5px; display: inline-block;'>
                    <span style='font-size: 32px; font-weight: bold; color: #922381; letter-spacing: 3px;'>$otp</span>
                </div>
            </div>
            
            <div style='background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;'>
                <h4 style='color: #922381; margin-top: 0;'>⚠️ Important Security Information:</h4>
                <ul style='margin: 0; padding-left: 20px; color: #555;'>
                    <li>This OTP is valid for <strong>10 minutes only</strong></li>
                    <li>Do not share this OTP with anyone</li>
                    <li>If you did not request this, please ignore this email</li>
                    <li>For security, this OTP can only be used once</li>
                </ul>
            </div>
            
            <div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>
                <p style='color: #666; font-size: 12px; margin: 0;'>
                    This email was sent from EduAlert Password Reset System<br>
                    If you need help, contact your system administrator
                </p>
            </div>
        </div>
    </body>
    </html>";
    
    // Plain text version for email clients that don't support HTML
    $text_message = "EduAlert - Password Reset OTP\n\n";
    $text_message .= "Dear $userName,\n\n";
    $text_message .= "You have requested to reset your password for your EduAlert account.\n\n";
    $text_message .= "Your One-Time Password (OTP) is: $otp\n\n";
    $text_message .= "Important:\n";
    $text_message .= "- This OTP is valid for 10 minutes only\n";
    $text_message .= "- Do not share this OTP with anyone\n";
    $text_message .= "- If you did not request this, please ignore this email\n\n";
    $text_message .= "Best regards,\nEduAlert Team";
    
    // Create email headers
    $headers = array();
    $headers[] = "MIME-Version: 1.0";
    $headers[] = "Content-Type: multipart/alternative; boundary=\"boundary-" . md5(time()) . "\"";
    $headers[] = "From: $from_name <$from_email>";
    $headers[] = "Reply-To: $from_email";
    $headers[] = "X-Mailer: EduAlert PHP Mailer";
    $headers[] = "X-Priority: 1";
    
    $boundary = "boundary-" . md5(time());
    
    $email_body = "--$boundary\r\n";
    $email_body .= "Content-Type: text/plain; charset=UTF-8\r\n";
    $email_body .= "Content-Transfer-Encoding: 7bit\r\n\r\n";
    $email_body .= $text_message . "\r\n";
    $email_body .= "--$boundary\r\n";
    $email_body .= "Content-Type: text/html; charset=UTF-8\r\n";
    $email_body .= "Content-Transfer-Encoding: 7bit\r\n\r\n";
    $email_body .= $html_message . "\r\n";
    $email_body .= "--$boundary--";
    
    // Update headers for multipart
    $headers_string = implode("\r\n", $headers) . "\r\n";
    $headers_string .= "Content-Type: multipart/alternative; boundary=\"$boundary\"\r\n";
    
    // Try to send using SMTP (fallback to mail() if SMTP fails)
    $smtp_sent = sendViaSMTP($email, $subject, $email_body, $smtp_host, $smtp_port, $smtp_username, $smtp_password, $from_email, $from_name);
    
    if ($smtp_sent) {
        return true;
    } else {
        // Fallback to PHP mail() function
        return mail($email, $subject, $html_message, $headers_string);
    }
}

/**
 * Send email via SMTP using socket connection
 */
function sendViaSMTP($to, $subject, $body, $host, $port, $username, $password, $from_email, $from_name) {
    try {
        // Create socket connection
        $socket = fsockopen($host, $port, $errno, $errstr, 30);
        if (!$socket) {
            return false;
        }
        
        // Read initial response
        $response = fgets($socket, 512);
        if (substr($response, 0, 3) != '220') {
            fclose($socket);
            return false;
        }
        
        // Send EHLO
        fputs($socket, "EHLO localhost\r\n");
        $response = fgets($socket, 512);
        
        // Start TLS
        fputs($socket, "STARTTLS\r\n");
        $response = fgets($socket, 512);
        if (substr($response, 0, 3) != '220') {
            fclose($socket);
            return false;
        }
        
        // Enable crypto
        if (!stream_socket_enable_crypto($socket, true, STREAM_CRYPTO_METHOD_TLS_CLIENT)) {
            fclose($socket);
            return false;
        }
        
        // Send EHLO again after TLS
        fputs($socket, "EHLO localhost\r\n");
        $response = fgets($socket, 512);
        
        // Authenticate
        fputs($socket, "AUTH LOGIN\r\n");
        $response = fgets($socket, 512);
        if (substr($response, 0, 3) != '334') {
            fclose($socket);
            return false;
        }
        
        fputs($socket, base64_encode($username) . "\r\n");
        $response = fgets($socket, 512);
        if (substr($response, 0, 3) != '334') {
            fclose($socket);
            return false;
        }
        
        fputs($socket, base64_encode($password) . "\r\n");
        $response = fgets($socket, 512);
        if (substr($response, 0, 3) != '235') {
            fclose($socket);
            return false;
        }
        
        // Send email
        fputs($socket, "MAIL FROM: <$from_email>\r\n");
        $response = fgets($socket, 512);
        
        fputs($socket, "RCPT TO: <$to>\r\n");
        $response = fgets($socket, 512);
        
        fputs($socket, "DATA\r\n");
        $response = fgets($socket, 512);
        
        // Send headers and body
        fputs($socket, "From: $from_name <$from_email>\r\n");
        fputs($socket, "To: <$to>\r\n");
        fputs($socket, "Subject: $subject\r\n");
        fputs($socket, "MIME-Version: 1.0\r\n");
        fputs($socket, $body . "\r\n");
        fputs($socket, ".\r\n");
        
        $response = fgets($socket, 512);
        $success = (substr($response, 0, 3) == '250');
        
        // Quit
        fputs($socket, "QUIT\r\n");
        fclose($socket);
        
        return $success;
        
    } catch (Exception $e) {
        return false;
    }
}
?>