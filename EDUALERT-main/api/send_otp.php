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

        // Clean up old OTPs for this email (optional - keep database clean)
        $cleanupStmt = $conn->prepare("DELETE FROM password_reset_otps WHERE email = ? AND expires_at < NOW()");
        $cleanupStmt->bind_param("s", $email);
        $cleanupStmt->execute();
        $cleanupStmt->close();

        // Insert new OTP
        $insertStmt = $conn->prepare("INSERT INTO password_reset_otps (email, otp, expires_at) VALUES (?, ?, ?)");
        $insertStmt->bind_param("sss", $email, $otp, $expiresAt);

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
 * Send OTP email using PHP's built-in mail function
 * For production, consider using PHPMailer or similar library
 */
function sendOtpEmail($email, $userName, $otp) {
    $subject = "EduAlert - Password Reset OTP";
    
    $message = "
    <html>
    <head>
        <title>Password Reset OTP</title>
    </head>
    <body>
        <div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>
            <h2 style='color: #922381;'>EduAlert - Password Reset</h2>
            <p>Dear $userName,</p>
            <p>You have requested to reset your password for your EduAlert account.</p>
            <p>Your One-Time Password (OTP) is:</p>
            <div style='background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; color: #922381; border-radius: 5px; margin: 20px 0;'>
                $otp
            </div>
            <p><strong>Important:</strong></p>
            <ul>
                <li>This OTP is valid for 10 minutes only</li>
                <li>Do not share this OTP with anyone</li>
                <li>If you did not request this, please ignore this email</li>
            </ul>
            <p>Best regards,<br>EduAlert Team</p>
        </div>
    </body>
    </html>
    ";

    // Email headers
    $headers = "MIME-Version: 1.0" . "\r\n";
    $headers .= "Content-type:text/html;charset=UTF-8" . "\r\n";
    $headers .= "From: EduAlert <noreply@edualert.com>" . "\r\n";
    $headers .= "Reply-To: support@edualert.com" . "\r\n";

    // Send email
    return mail($email, $subject, $message, $headers);
}
?>