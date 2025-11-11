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

        // Verify OTP
        $otpStmt = $conn->prepare("SELECT id, attempts FROM password_reset_otps WHERE email = ? AND otp = ? AND expires_at > NOW() AND is_used = 0");
        $otpStmt->bind_param("ss", $email, $otp);
        $otpStmt->execute();
        $otpResult = $otpStmt->get_result();

        if ($otpResult->num_rows === 0) {
            // Increment attempts for this email/OTP combination
            $attemptStmt = $conn->prepare("UPDATE password_reset_otps SET attempts = attempts + 1 WHERE email = ? AND otp = ?");
            $attemptStmt->bind_param("ss", $email, $otp);
            $attemptStmt->execute();
            $attemptStmt->close();

            $response['status'] = 'error';
            $response['message'] = 'Invalid or expired OTP. Please request a new one.';
            echo json_encode($response);
            exit;
        }

        $otpData = $otpResult->fetch_assoc();
        $otpId = $otpData['id'];
        $attempts = $otpData['attempts'];
        $otpStmt->close();

        // Check if too many attempts
        if ($attempts >= 5) {
            $response['status'] = 'error';
            $response['message'] = 'Too many failed attempts. Please request a new OTP.';
            echo json_encode($response);
            exit;
        }

        // Check if user exists
        $userStmt = $conn->prepare("SELECT user_id FROM users WHERE email = ?");
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

        // Hash the new password
        $hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);

        // Update user password
        $updateStmt = $conn->prepare("UPDATE users SET password = ? WHERE email = ?");
        $updateStmt->bind_param("ss", $hashedPassword, $email);

        if ($updateStmt->execute()) {
            // Mark OTP as used
            $markUsedStmt = $conn->prepare("UPDATE password_reset_otps SET is_used = 1 WHERE id = ?");
            $markUsedStmt->bind_param("i", $otpId);
            $markUsedStmt->execute();
            $markUsedStmt->close();

            // Clean up old OTPs for this email
            $cleanupStmt = $conn->prepare("DELETE FROM password_reset_otps WHERE email = ? AND id != ?");
            $cleanupStmt->bind_param("si", $email, $otpId);
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
 * Send password reset confirmation email
 */
function sendPasswordResetConfirmation($email) {
    $subject = "EduAlert - Password Reset Successful";
    
    $message = "
    <html>
    <head>
        <title>Password Reset Confirmation</title>
    </head>
    <body>
        <div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>
            <h2 style='color: #922381;'>EduAlert - Password Reset Successful</h2>
            <p>Your password has been successfully reset.</p>
            <p>You can now login to your EduAlert account using your new password.</p>
            <p><strong>Security Tips:</strong></p>
            <ul>
                <li>Keep your password secure and don't share it with anyone</li>
                <li>Use a strong password with a mix of letters, numbers, and symbols</li>
                <li>If you didn't perform this action, please contact support immediately</li>
            </ul>
            <p>Best regards,<br>EduAlert Team</p>
        </div>
    </body>
    </html>
    ";

    $headers = "MIME-Version: 1.0" . "\r\n";
    $headers .= "Content-type:text/html;charset=UTF-8" . "\r\n";
    $headers .= "From: EduAlert <noreply@edualert.com>" . "\r\n";

    return mail($email, $subject, $message, $headers);
}
?>