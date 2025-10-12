<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

include('db.php');

$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $title = isset($_POST['title']) ? trim($_POST['title']) : '';
    $message = isset($_POST['message']) ? trim($_POST['message']) : '';
    $user_type = isset($_POST['user_type']) ? trim($_POST['user_type']) : '';
    $user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
    
    if (empty($title) || empty($message) || empty($user_type) || empty($user_id)) {
        echo json_encode([
            "status" => "error",
            "message" => "All fields are required."
        ]);
        exit;
    }
    
    // Validate user type
    if (!in_array($user_type, ['student', 'staff', 'admin'])) {
        echo json_encode([
            "status" => "error",
            "message" => "Invalid user type."
        ]);
        exit;
    }
    
    // Insert notification
    $sql = "
        INSERT INTO notifications (title, message, user_type, user_id, status, created_at) 
        VALUES (?, ?, ?, ?, 'unread', NOW())
    ";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        echo json_encode([
            "status" => "error",
            "message" => "Database error: " . $conn->error
        ]);
        exit;
    }
    
    $stmt->bind_param("ssss", $title, $message, $user_type, $user_id);
    
    if ($stmt->execute()) {
        echo json_encode([
            "status" => "success",
            "message" => "Notification created successfully.",
            "notification_id" => $conn->insert_id
        ]);
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Failed to create notification."
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

