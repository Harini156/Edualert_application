<?php
// Mark Message Status API - Handle read/unread/delete status for individual users
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
        $user_id = trim($_POST['user_id'] ?? '');
        $message_id = trim($_POST['message_id'] ?? '');
        $message_table = trim($_POST['message_table'] ?? ''); // 'messages' or 'staffmessages'
        $status = trim($_POST['status'] ?? ''); // 'read', 'unread', 'deleted'

        // Validation
        if (empty($user_id) || empty($message_id) || empty($message_table) || empty($status)) {
            $response['status'] = 'error';
            $response['message'] = 'All fields are required (user_id, message_id, message_table, status).';
            echo json_encode($response);
            exit;
        }

        if (!in_array($message_table, ['messages', 'staffmessages'])) {
            $response['status'] = 'error';
            $response['message'] = 'Invalid message_table. Must be "messages" or "staffmessages".';
            echo json_encode($response);
            exit;
        }

        if (!in_array($status, ['read', 'unread', 'deleted'])) {
            $response['status'] = 'error';
            $response['message'] = 'Invalid status. Must be "read", "unread", or "deleted".';
            echo json_encode($response);
            exit;
        }

        // Check if record already exists
        $check_stmt = $conn->prepare("SELECT id FROM user_message_status WHERE user_id = ? AND message_id = ? AND message_table = ?");
        $check_stmt->bind_param("sis", $user_id, $message_id, $message_table);
        $check_stmt->execute();
        $check_result = $check_stmt->get_result();
        $check_stmt->close();

        if ($check_result->num_rows > 0) {
            // UPDATE existing record
            $update_stmt = $conn->prepare("UPDATE user_message_status SET status = ?, marked_at = NOW() WHERE user_id = ? AND message_id = ? AND message_table = ?");
            $update_stmt->bind_param("ssis", $status, $user_id, $message_id, $message_table);
            
            if ($update_stmt->execute()) {
                $response['status'] = 'success';
                $response['message'] = 'Message status updated successfully.';
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Failed to update message status: ' . $update_stmt->error;
            }
            $update_stmt->close();
        } else {
            // INSERT new record
            $insert_stmt = $conn->prepare("INSERT INTO user_message_status (user_id, message_id, message_table, status, marked_at) VALUES (?, ?, ?, ?, NOW())");
            $insert_stmt->bind_param("siss", $user_id, $message_id, $message_table, $status);
            
            if ($insert_stmt->execute()) {
                $response['status'] = 'success';
                $response['message'] = 'Message status created successfully.';
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Failed to create message status: ' . $insert_stmt->error;
            }
            $insert_stmt->close();
        }

    } else {
        $response['status'] = 'error';
        $response['message'] = 'Invalid request method. Expected POST.';
    }
} catch (Exception $e) {
    $response['status'] = 'error';
    $response['message'] = 'Server error occurred: ' . $e->getMessage();
}

if (isset($conn)) {
    $conn->close();
}

echo json_encode($response);
?>