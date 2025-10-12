<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

include('db.php');

$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $user_type = isset($_POST['user_type']) ? trim($_POST['user_type']) : '';
    $user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
    
    if (empty($user_type) || empty($user_id)) {
        echo json_encode([
            "status" => "error",
            "message" => "User type and user ID are required."
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
    
    // Get unread notification count for the specific user
    $sql = "
        SELECT COUNT(*) as unread_count 
        FROM notifications 
        WHERE user_type = ? AND user_id = ? AND status = 'unread'
    ";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        echo json_encode([
            "status" => "error",
            "message" => "Database error: " . $conn->error
        ]);
        exit;
    }
    
    $stmt->bind_param("ss", $user_type, $user_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $row = $result->fetch_assoc();
    
    echo json_encode([
        "status" => "success",
        "unread_count" => (int)$row['unread_count']
    ]);
    
    $stmt->close();
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid request method."
    ]);
}

$conn->close();
?>

