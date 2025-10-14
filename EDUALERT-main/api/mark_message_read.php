<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

include('db.php');

$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Debug: Log received data
    error_log("Received POST data: " . print_r($_POST, true));
    
    $message_id = isset($_POST['message_id']) ? (int)$_POST['message_id'] : 0;
    $table_name = isset($_POST['table_name']) ? trim($_POST['table_name']) : '';
    
    // Debug: Log parsed values
    error_log("Parsed message_id: $message_id, table_name: $table_name");
    
    if ($message_id <= 0 || empty($table_name)) {
        echo json_encode([
            "status" => "error",
            "message" => "Valid message ID and table name are required.",
            "debug" => [
                "message_id" => $message_id,
                "table_name" => $table_name,
                "raw_post" => $_POST
            ]
        ]);
        exit;
    }
    
    // Validate table name
    if (!in_array($table_name, ['messages', 'staffmessages'])) {
        echo json_encode([
            "status" => "error",
            "message" => "Invalid table name. Use 'messages' or 'staffmessages'.",
            "debug" => [
                "received_table" => $table_name
            ]
        ]);
        exit;
    }
    
    // Update message status to read
    $sql = "UPDATE $table_name SET status = 'read' WHERE id = ?";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        echo json_encode([
            "status" => "error",
            "message" => "Database error: " . $conn->error,
            "debug" => [
                "sql" => $sql,
                "error" => $conn->error
            ]
        ]);
        exit;
    }
    
    $stmt->bind_param("i", $message_id);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            echo json_encode([
                "status" => "success",
                "message" => "Message marked as read.",
                "debug" => [
                    "affected_rows" => $stmt->affected_rows,
                    "message_id" => $message_id,
                    "table_name" => $table_name
                ]
            ]);
        } else {
            echo json_encode([
                "status" => "error",
                "message" => "Message not found or already read.",
                "debug" => [
                    "affected_rows" => $stmt->affected_rows,
                    "message_id" => $message_id,
                    "table_name" => $table_name
                ]
            ]);
        }
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Failed to update message: " . $stmt->error,
            "debug" => [
                "sql_error" => $stmt->error,
                "message_id" => $message_id,
                "table_name" => $table_name
            ]
        ]);
    }
    
    $stmt->close();
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid request method. Expected POST, got " . $_SERVER['REQUEST_METHOD']
    ]);
}

$conn->close();
?>
