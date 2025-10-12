<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

include('db.php');

$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $notification_id = isset($_POST['notification_id']) ? (int)$_POST['notification_id'] : 0;
    
    if ($notification_id <= 0) {
        echo json_encode([
            "status" => "error",
            "message" => "Valid notification ID is required."
        ]);
        exit;
    }
    
    // Update notification status to read
    $sql = "
        UPDATE notifications 
        SET status = 'read' 
        WHERE id = ?
    ";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        echo json_encode([
            "status" => "error",
            "message" => "Database error: " . $conn->error
        ]);
        exit;
    }
    
    $stmt->bind_param("i", $notification_id);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            echo json_encode([
                "status" => "success",
                "message" => "Notification marked as read."
            ]);
        } else {
            echo json_encode([
                "status" => "error",
                "message" => "Notification not found."
            ]);
        }
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Failed to update notification."
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

