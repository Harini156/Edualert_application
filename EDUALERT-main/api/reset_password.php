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
        $otp = trim($_POST['otp'] ?? '');
        $newPassword = trim($_POST['new_password'] ?? '');

        // Validate input
        if (empty($email) || empty($otp) || empty($newPassword)) {
            $response['status'] = 'error';
            $response['message'] = 'Email, OTP, and new password are required.';
            echo json_encode($response);
            exit;
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $response['status'] = 'error';
            $response['message'] = 'Invalid email format.';
            echo json_encode($response);
            exit;
        }

        if (strlen($newPassword) < 6) {
            $response['status'] = 'error';
            $response['message'] = 'Password must be at least 6 characters long.';
            echo json_encode($response);
            exit;
        }

        // Get user ID first
        $userStmt = $conn->prepare("SELECT id, user_id FROM users WHERE email = ?");
        $userStmt->bind_param("s", $email);
        $userStmt->execute();
        $userResult = $userStmt->get_result();

        if ($userResult->num_rows === 0) {
            $response['status'] = 'error';
            $response['message'] = 'User account not found.';
            echo json_encode($response);
            exit;
        }

        $user = $userResult->fetch_assoc();
        $userId = $user['user_id'];
        $userStmt->close();

        // Verify OTP using existing table structure
        $otpStmt = $conn->prepare("SELECT id FROM password_reset WHERE user_id = ? AND otp = ? AND expiry > NOW()");
        $otpStmt->bind_param("ss", $userId, $otp);
        $otpStmt->execute();
        $otpResult = $otpStmt->get_result();

        if ($otpResult->num_rows === 0) {
            $response['status'] = 'error';
            $response['message'] = 'Invalid or expired OTP. Please request a new one.';
            echo json_encode($response);
            exit;
        }

        $otpData = $otpResult->fetch_assoc();
        $otpId = $otpData['id'];
        $otpStmt->close();



        // Hash the new password
        $hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);

        // Update user password
        $updateStmt = $conn->prepare("UPDATE users SET password = ? WHERE email = ?");
        $updateStmt->bind_param("ss", $hashedPassword, $email);

        if ($updateStmt->execute()) {
            // Delete the used OTP from password_reset table
            $deleteOtpStmt = $conn->prepare("DELETE FROM password_reset WHERE id = ?");
            $deleteOtpStmt->bind_param("i", $otpId);
            $deleteOtpStmt->execute();
            $deleteOtpStmt->close();

            // Clean up any other expired OTPs for this user
            $cleanupStmt = $conn->prepare("DELETE FROM password_reset WHERE user_id = ? AND expiry < NOW()");
            $cleanupStmt->bind_param("s", $userId);
            $cleanupStmt->execute();
            $cleanupStmt->close();

            $response['status'] = 'success';
            $response['message'] = 'Password reset successfully. You can now login with your new password.';

            // Send confirmation email
            sendPasswordResetConfirmation($email);
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Failed to update password. Please try again.';
        }

        $updateStmt->close();
    } else {
        $response['status'] = 'error';
        $response['message'] = 'Invalid request method.';
    }
} catch (Exception $e) {
    $response['status'] = 'error';
    $response['message'] = 'Server error occurred. Please try again later.';
    error_log("Reset Password Error: " . $e->getMessage());
}

if (isset($conn)) {
    $conn->close();
}

echo json_encode($response);

/**
 * Send password reset confirmation email using PHPMailer - PRODUCTION READY
 */
function sendPasswordResetConfirmation($email) {
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
        $mail->addAddress($email);
        $mail->addReplyTo('edualert.notifications@gmail.com', 'EduAlert Support');
        
        // Email content
        $mail->isHTML(true);
        $mail->Subject = 'EduAlert - Password Reset Successful';
        
        // Professional HTML email body
        $mail->Body = "
        <html>
        <head>
            <title>Password Reset Confirmation</title>
        </head>
        <body>
            <div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>
                <div style='text-align: center; margin-bottom: 30px;'>
                    <h2 style='color: #922381; margin: 0;'>✅ EduAlert</h2>
                    <p style='color: #666; margin: 5px 0;'>Password Reset Successful</p>
                </div>
                
                <div style='background: #e8f5e8; padding: 20px; border-radius: 8px; border-left: 4px solid #4caf50; margin: 20px 0;'>
                    <h3 style='color: #4caf50; margin-top: 0;'>🎉 Success!</h3>
                    <p style='margin: 0; color: #333;'>Your password has been successfully reset.</p>
                </div>
                
                <p style='font-size: 16px; color: #333;'>
                    You can now login to your EduAlert account using your new password.
                </p>
                
                <div style='background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;'>
                    <h4 style='color: #922381; margin-top: 0;'>🔒 Security Tips:</h4>
                    <ul style='margin: 0; padding-left: 20px; color: #555;'>
                        <li>Keep your password secure and don't share it with anyone</li>
                        <li>Use a strong password with a mix of letters, numbers, and symbols</li>
                        <li>If you didn't perform this action, please contact support immediately</li>
                        <li>Consider enabling two-factor authentication for extra security</li>
                    </ul>
                </div>
                
                <div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>
                    <p style='color: #666; font-size: 12px; margin: 0;'>
                        This email was sent from EduAlert Security System<br>
                        If you need help, contact your system administrator
                    </p>
                </div>
            </div>
        </body>
        </html>";
        
        // Plain text alternative
        $mail->AltBody = "Your password has been successfully reset.\n\nYou can now login to your EduAlert account using your new password.\n\nIf you didn't perform this action, please contact support immediately.\n\nBest regards,\nEduAlert System";
        
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