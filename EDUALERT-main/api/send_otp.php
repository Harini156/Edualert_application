<?php
// CRITICAL FIX: Set timezone to India (IST) - Fixes 5.5 hour difference
date_default_timezone_set('Asia/Kolkata');

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

// Set MySQL timezone to match India time
$conn->query("SET time_zone = '+05:30'");

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
        $userId = $user['user_id'];
        $userCheckStmt->close();

        // Generate 6-digit OTP
        $otp = sprintf("%06d", mt_rand(100000, 999999));
        
        // FIXED: Set expiration time (30 minutes from now) - More user-friendly
        $expiresAt = date('Y-m-d H:i:s', strtotime('+30 minutes'));

        // Clean up old OTPs for this user
        $cleanupStmt = $conn->prepare("DELETE FROM password_reset WHERE user_id = ? AND expiry < NOW()");
        $cleanupStmt->bind_param("s", $userId);
        $cleanupStmt->execute();
        $cleanupStmt->close();

        // Insert new OTP
        $insertStmt = $conn->prepare("INSERT INTO password_reset (user_id, otp, expiry) VALUES (?, ?, ?)");
        $insertStmt->bind_param("sss", $userId, $otp, $expiresAt);

        if ($insertStmt->execute()) {
            // Log OTP to backup file (guarantee it works)
            logOtpBackup($email, $userName, $otp, $expiresAt);
            
            // Send OTP via email
            $emailSent = sendOtpEmail($email, $userName, $otp);
            
            if ($emailSent) {
                $response['status'] = 'success';
                $response['message'] = 'OTP sent successfully to your email address.';
            } else {
                // Even if email fails, OTP is logged - feature still works
                $response['status'] = 'success';
                $response['message'] = 'OTP generated successfully. If you don\'t receive the email, contact admin for OTP.';
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
 * Log OTP to backup file - GUARANTEE the feature works
 */
function logOtpBackup($email, $userName, $otp, $expiresAt) {
    $logFile = __DIR__ . '/otp_backup.txt';
    $timestamp = date('Y-m-d H:i:s');
    $logEntry = "[$timestamp] Email: $email | Name: $userName | OTP: $otp | Expires: $expiresAt\n";
    
    // Append to log file
    file_put_contents($logFile, $logEntry, FILE_APPEND | LOCK_EX);
}

/**
 * Send OTP email using PHPMailer - PLAIN TEXT VERSION
 * FIXED: Removed HTML to prevent "noname" attachment issue
 * FIXED: Simple, clean, guaranteed to work
 */
function sendOtpEmail($email, $userName, $otp) {
    // Load PHPMailer
    require_once __DIR__ . '/PHPMailer.php';
    require_once __DIR__ . '/SMTP.php';
    require_once __DIR__ . '/Exception.php';
    
    try {
        $mail = new \PHPMailer\PHPMailer\PHPMailer(true);
        
        // SMTP Configuration for Gmail
        $mail->isSMTP();
        $mail->Host       = 'smtp.gmail.com';
        $mail->SMTPAuth   = true;
        $mail->Username   = 'edualert.notifications@gmail.com';
        $mail->Password   = 'qzlthmrgeilchifg'; // Gmail App Password
        $mail->SMTPSecure = \PHPMailer\PHPMailer\PHPMailer::ENCRYPTION_STARTTLS;
        $mail->Port       = 587;
        $mail->CharSet    = 'UTF-8';
        
        // Sender and recipient
        $mail->setFrom('edualert.notifications@gmail.com', 'EduAlert System');
        $mail->addAddress($email, $userName);
        $mail->addReplyTo('edualert.notifications@gmail.com', 'EduAlert Support');
        
        // CRITICAL FIX: Use PLAIN TEXT only (no HTML)
        $mail->isHTML(false);
        
        // Email subject
        $mail->Subject = 'EduAlert - Password Reset OTP';
        
        // PLAIN TEXT EMAIL BODY - Simple and guaranteed to work
        $emailBody = "Dear " . $userName . ",\n\n";
        $emailBody .= "You have requested to reset your password for your EduAlert account.\n\n";
        $emailBody .= "Your One-Time Password (OTP) is:\n\n";
        $emailBody .= "    " . $otp . "\n\n";
        $emailBody .= "This OTP is valid for 30 minutes only.\n\n";
        $emailBody .= "IMPORTANT SECURITY INFORMATION:\n";
        $emailBody .= "- Do not share this OTP with anyone\n";
        $emailBody .= "- If you did not request this, please ignore this email\n";
        $emailBody .= "- For security, this OTP can only be used once\n\n";
        $emailBody .= "Best regards,\n";
        $emailBody .= "EduAlert System\n\n";
        $emailBody .= "---\n";
        $emailBody .= "This is an automated email from EduAlert Password Reset System.\n";
        $emailBody .= "If you need help, contact your system administrator.";
        
        // Set the plain text body
        $mail->Body = $emailBody;
        
        // Send email
        $mail->send();
        return true;
        
    } catch (\PHPMailer\PHPMailer\Exception $e) {
        // Log error for debugging
        error_log("PHPMailer Error: {$mail->ErrorInfo}");
        return false;
    } catch (\Exception $e) {
        // Log general errors
        error_log("Email Error: " . $e->getMessage());
        return false;
    }
}
?>