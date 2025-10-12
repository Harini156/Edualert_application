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
    
    // Get notifications for the specific user
    $sql = "
        SELECT id, title, message, user_type, user_id, status, created_at 
        FROM notifications 
        WHERE user_type = ? AND user_id = ? 
        ORDER BY created_at DESC
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
    
    $notifications = [];
    
    while ($row = $result->fetch_assoc()) {
        $notifications[] = $row;
    }
    
    echo json_encode([
        "status" => "success",
        "notifications" => $notifications,
        "total_count" => count($notifications)
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

