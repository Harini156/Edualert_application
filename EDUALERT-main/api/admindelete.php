<?php
// Admin Delete Message API - Allow admin to delete their sent messages
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
        $message_id = trim($_POST['id'] ?? '');
        $user_type = trim($_POST['usertype'] ?? '');

        // Validation
        if (empty($message_id) || empty($user_type)) {
            $response['status'] = 'error';
            $response['message'] = 'Message ID and user type are required.';
            echo json_encode($response);
            exit;
        }

        // Validate message ID is numeric
        if (!is_numeric($message_id)) {
            $response['status'] = 'error';
            $response['message'] = 'Invalid message ID format.';
            echo json_encode($response);
            exit;
        }

        // Validate user type
        if (!in_array($user_type, ['admin', 'staff'])) {
            $response['status'] = 'error';
            $response['message'] = 'Invalid user type. Must be "admin" or "staff".';
            echo json_encode($response);
            exit;
        }

        // Determine which table to delete from based on user type
        if ($user_type === 'admin') {
            // Delete from messages table (admin messages)
            $table = 'messages';
        } else {
            // Delete from staffmessages table (staff messages)
            $table = 'staffmessages';
        }

        // Check if message exists before deletion
        $check_stmt = $conn->prepare("SELECT id FROM $table WHERE id = ?");
        $check_stmt->bind_param("i", $message_id);
        $check_stmt->execute();
        $check_result = $check_stmt->get_result();

        if ($check_result->num_rows == 0) {
            $response['status'] = 'error';
            $response['message'] = 'Message not found.';
            echo json_encode($response);
            exit;
        }
        $check_stmt->close();

        // Delete the message
        $delete_stmt = $conn->prepare("DELETE FROM $table WHERE id = ?");
        $delete_stmt->bind_param("i", $message_id);

        if ($delete_stmt->execute()) {
            if ($delete_stmt->affected_rows > 0) {
                $response['status'] = 'success';
                $response['message'] = 'Message deleted successfully.';
                $response['deleted_id'] = $message_id;
                $response['table'] = $table;
            } else {
                $response['status'] = 'error';
                $response['message'] = 'No message was deleted. Message may not exist.';
            }
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Failed to delete message: ' . $delete_stmt->error;
        }

        $delete_stmt->close();

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