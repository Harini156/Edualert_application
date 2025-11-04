<?php
// Staff Delete Message API - Allow staff to delete their sent messages
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
        $message_id = trim($_POST['message_id'] ?? '');
        $sender_id = trim($_POST['sender_id'] ?? '');

        // Validation
        if (empty($message_id) || empty($sender_id)) {
            $response['status'] = 'error';
            $response['message'] = 'Message ID and sender ID are required.';
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

        // Check if message exists and belongs to the sender
        $check_stmt = $conn->prepare("SELECT id, sender_id FROM staffmessages WHERE id = ? AND sender_id = ?");
        $check_stmt->bind_param("is", $message_id, $sender_id);
        $check_stmt->execute();
        $check_result = $check_stmt->get_result();

        if ($check_result->num_rows == 0) {
            $response['status'] = 'error';
            $response['message'] = 'Message not found or you do not have permission to delete this message.';
            echo json_encode($response);
            exit;
        }
        $check_stmt->close();

        // Delete the message
        $delete_stmt = $conn->prepare("DELETE FROM staffmessages WHERE id = ? AND sender_id = ?");
        $delete_stmt->bind_param("is", $message_id, $sender_id);

        if ($delete_stmt->execute()) {
            if ($delete_stmt->affected_rows > 0) {
                $response['status'] = true; // Note: Staff delete uses boolean status
                $response['message'] = 'Message deleted successfully.';
                $response['deleted_id'] = $message_id;
            } else {
                $response['status'] = false;
                $response['message'] = 'No message was deleted. Message may not exist or you do not have permission.';
            }
        } else {
            $response['status'] = false;
            $response['message'] = 'Failed to delete message: ' . $delete_stmt->error;
        }

        $delete_stmt->close();

    } else {
        $response['status'] = false;
        $response['message'] = 'Invalid request method. Expected POST.';
    }
} catch (Exception $e) {
    $response['status'] = false;
    $response['message'] = 'Server error occurred.';
    $response['debug'] = $e->getMessage();
}

if (isset($conn)) {
    $conn->close();
}

echo json_encode($response);
?>