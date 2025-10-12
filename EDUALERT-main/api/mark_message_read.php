<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

include('db.php');

$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $message_id = isset($_POST['message_id']) ? (int)$_POST['message_id'] : 0;
    $table_name = isset($_POST['table_name']) ? trim($_POST['table_name']) : '';
    
    if ($message_id <= 0 || empty($table_name)) {
        echo json_encode([
            "status" => "error",
            "message" => "Valid message ID and table name are required."
        ]);
        exit;
    }
    
    // Validate table name
    if (!in_array($table_name, ['messages', 'staffmessages'])) {
        echo json_encode([
            "status" => "error",
            "message" => "Invalid table name. Use 'messages' or 'staffmessages'."
        ]);
        exit;
    }
    
    // Update message status to read
    $sql = "UPDATE $table_name SET status = 'read' WHERE id = ?";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        echo json_encode([
            "status" => "error",
            "message" => "Database error: " . $conn->error
        ]);
        exit;
    }
    
    $stmt->bind_param("i", $message_id);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            echo json_encode([
                "status" => "success",
                "message" => "Message marked as read."
            ]);
        } else {
            echo json_encode([
                "status" => "error",
                "message" => "Message not found."
            ]);
        }
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Failed to update message."
        ]);
    }
    
    $stmt->close();
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid request method."
    ]);
}

$conn->close();
?>
