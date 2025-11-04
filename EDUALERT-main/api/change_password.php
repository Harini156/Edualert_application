<?php
// Change Password API - Allow users to change their password
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

include 'db.php';

$response = [];

try {
    if ($_SERVER["REQUEST_METHOD"] == "POST") {
        $email = trim($_POST['email'] ?? '');
        $old_password = $_POST['old_password'] ?? '';
        $new_password = $_POST['new_password'] ?? '';

        // Validation
        if (empty($email) || empty($old_password) || empty($new_password)) {
            $response['status'] = 'error';
            $response['message'] = 'All fields are required (email, old_password, new_password).';
            echo json_encode($response);
            exit;
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $response['status'] = 'error';
            $response['message'] = 'Invalid email format.';
            echo json_encode($response);
            exit;
        }

        if (strlen($new_password) < 6) {
            $response['status'] = 'error';
            $response['message'] = 'New password must be at least 6 characters long.';
            echo json_encode($response);
            exit;
        }

        // Get user from database
        $stmt = $conn->prepare("SELECT id, user_id, name, password, user_type FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows == 0) {
            $response['status'] = 'error';
            $response['message'] = 'User not found with this email.';
            echo json_encode($response);
            exit;
        }

        $user = $result->fetch_assoc();
        $stmt->close();

        // Verify old password
        if (!password_verify($old_password, $user['password'])) {
            $response['status'] = 'error';
            $response['message'] = 'Current password is incorrect.';
            echo json_encode($response);
            exit;
        }

        // Check if new password is same as old password
        if (password_verify($new_password, $user['password'])) {
            $response['status'] = 'error';
            $response['message'] = 'New password must be different from current password.';
            echo json_encode($response);
            exit;
        }

        // Hash new password
        $hashed_new_password = password_hash($new_password, PASSWORD_DEFAULT);

        // Update password in database
        $update_stmt = $conn->prepare("UPDATE users SET password = ? WHERE email = ?");
        $update_stmt->bind_param("ss", $hashed_new_password, $email);

        if ($update_stmt->execute()) {
            $response['status'] = 'success';
            $response['message'] = 'Password changed successfully.';
            $response['user_info'] = [
                'user_id' => $user['user_id'],
                'name' => $user['name'],
                'email' => $email,
                'user_type' => $user['user_type']
            ];
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Failed to update password. Please try again.';
        }

        $update_stmt->close();

    } else {
        $response['status'] = 'error';
        $response['message'] = 'Invalid request method. Expected POST.';
    }
} catch (Exception $e) {
    $response['status'] = 'error';
    $response['message'] = 'Server error occurred.';
    $response['debug'] = $e->getMessage();
}

if (isset($conn)) {
    $conn->close();
}

echo json_encode($response);
?>