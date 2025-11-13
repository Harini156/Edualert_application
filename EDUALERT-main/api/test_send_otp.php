<?php
// Test Send OTP - Debug version to identify the exact issue
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$response = [];
$debug = [];

try {
    // Test 1: Check if POST data is received
    $debug['step1_post_data'] = $_POST;
    $debug['step1_request_method'] = $_SERVER['REQUEST_METHOD'];
    
    if ($_SERVER["REQUEST_METHOD"] == "POST") {
        $email = trim($_POST['email'] ?? '');
        $debug['step2_email_received'] = $email;
        
        // Test 2: Basic validation
        if (empty($email)) {
            $response['status'] = 'error';
            $response['message'] = 'Email is required.';
            $response['debug'] = $debug;
            echo json_encode($response);
            exit;
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $response['status'] = 'error';
            $response['message'] = 'Invalid email format.';
            $response['debug'] = $debug;
            echo json_encode($response);
            exit;
        }
        
        $debug['step3_email_validation'] = 'passed';

        // Test 3: Database connection
        try {
            include 'db.php';
            $debug['step4_db_connection'] = 'success';
        } catch (Exception $e) {
            $response['status'] = 'error';
            $response['message'] = 'Database connection failed: ' . $e->getMessage();
            $response['debug'] = $debug;
            echo json_encode($response);
            exit;
        }

        // Test 4: Check if user exists
        try {
            $userCheckStmt = $conn->prepare("SELECT user_id, name FROM users WHERE email = ?");
            $userCheckStmt->bind_param("s", $email);
            $userCheckStmt->execute();
            $userResult = $userCheckStmt->get_result();
            $debug['step5_user_query'] = 'executed';
            $debug['step5_user_found'] = $userResult->num_rows > 0;

            if ($userResult->num_rows === 0) {
                $response['status'] = 'error';
                $response['message'] = 'No account found with this email address.';
                $response['debug'] = $debug;
                echo json_encode($response);
                exit;
            }

            $user = $userResult->fetch_assoc();
            $userName = $user['name'];
            $userId = $user['user_id'];
            $userCheckStmt->close();
            $debug['step6_user_data'] = ['user_id' => $userId, 'name' => $userName];
        } catch (Exception $e) {
            $response['status'] = 'error';
            $response['message'] = 'User query failed: ' . $e->getMessage();
            $response['debug'] = $debug;
            echo json_encode($response);
            exit;
        }

        // Test 5: Check password_reset table
        try {
            $tableCheckStmt = $conn->prepare("DESCRIBE password_reset");
            $tableCheckStmt->execute();
            $tableResult = $tableCheckStmt->get_result();
            $debug['step7_table_exists'] = $tableResult->num_rows > 0;
            $tableCheckStmt->close();
        } catch (Exception $e) {
            $response['status'] = 'error';
            $response['message'] = 'Password reset table check failed: ' . $e->getMessage();
            $response['debug'] = $debug;
            echo json_encode($response);
            exit;
        }

        // Test 6: Generate OTP and try to insert
        try {
            $otp = sprintf("%06d", mt_rand(100000, 999999));
            $expiresAt = date('Y-m-d H:i:s', strtotime('+10 minutes'));
            $debug['step8_otp_generated'] = $otp;
            $debug['step8_expires_at'] = $expiresAt;

            // Clean up old OTPs
            $cleanupStmt = $conn->prepare("DELETE FROM password_reset WHERE user_id = ? AND expiry < NOW()");
            $cleanupStmt->bind_param("s", $userId);
            $cleanupStmt->execute();
            $cleanupStmt->close();
            $debug['step9_cleanup'] = 'completed';

            // Insert new OTP
            $insertStmt = $conn->prepare("INSERT INTO password_reset (user_id, otp, expiry) VALUES (?, ?, ?)");
            $insertStmt->bind_param("sss", $userId, $otp, $expiresAt);
            
            if ($insertStmt->execute()) {
                $debug['step10_otp_inserted'] = 'success';
                $insertStmt->close();
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Failed to insert OTP: ' . $insertStmt->error;
                $response['debug'] = $debug;
                echo json_encode($response);
                exit;
            }
        } catch (Exception $e) {
            $response['status'] = 'error';
            $response['message'] = 'OTP generation failed: ' . $e->getMessage();
            $response['debug'] = $debug;
            echo json_encode($response);
            exit;
        }

        // Test 7: Email sending (this is likely where it fails)
        try {
            $debug['step11_email_function_exists'] = function_exists('mail');
            
            if (function_exists('mail')) {
                // Try to send email
                $subject = "EduAlert - Password Reset OTP (TEST)";
                $message = "Your OTP is: $otp (This is a test email)";
                $headers = "From: noreply@edualert.com";
                
                $emailSent = mail($email, $subject, $message, $headers);
                $debug['step12_email_sent'] = $emailSent;
                
                if ($emailSent) {
                    $response['status'] = 'success';
                    $response['message'] = 'OTP sent successfully to your email address.';
                } else {
                    $response['status'] = 'error';
                    $response['message'] = 'OTP generated but failed to send email. Email function not working on server.';
                }
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Email function not available on server. Server admin needs to configure email.';
            }
            
            $response['debug'] = $debug;
        } catch (Exception $e) {
            $response['status'] = 'error';
            $response['message'] = 'Email sending failed: ' . $e->getMessage();
            $response['debug'] = $debug;
        }

    } else {
        $response['status'] = 'error';
        $response['message'] = 'Invalid request method.';
        $response['debug'] = $debug;
    }
} catch (Exception $e) {
    $response['status'] = 'error';
    $response['message'] = 'General server error: ' . $e->getMessage();
    $response['debug'] = $debug;
}

if (isset($conn)) {
    $conn->close();
}

echo json_encode($response, JSON_PRETTY_PRINT);
?>