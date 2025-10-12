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
    
    $total_unread_count = 0;
    
    // Count from messages table (Admin messages)
    $messages_count = 0;
    $messages_sql = "SELECT COUNT(*) as count FROM messages WHERE recipient_type = ? AND status = 'unread'";
    $stmt = $conn->prepare($messages_sql);
    if ($stmt) {
        $stmt->bind_param("s", $user_type);
        $stmt->execute();
        $result = $stmt->get_result();
        $row = $result->fetch_assoc();
        $messages_count = (int)$row['count'];
        $stmt->close();
    }
    
    // Count from staffmessages table (Staff messages)
    $staffmessages_count = 0;
    
    if ($user_type === 'student') {
        // For students: count messages sent to them specifically or to groups they belong to
        $staffmessages_sql = "
            SELECT COUNT(*) as count 
            FROM staffmessages 
            WHERE status = 'unread' 
            AND (
                (receiver_id = ? AND is_group_message = 0) 
                OR 
                (receiver_id IN (
                    SELECT group_id FROM group_members WHERE user_id = ?
                ) AND is_group_message = 1)
            )
        ";
        $stmt = $conn->prepare($staffmessages_sql);
        if ($stmt) {
            $stmt->bind_param("ss", $user_id, $user_id);
            $stmt->execute();
            $result = $stmt->get_result();
            $row = $result->fetch_assoc();
            $staffmessages_count = (int)$row['count'];
            $stmt->close();
        }
        
    } elseif ($user_type === 'staff') {
        // For staff: count messages sent to staff type
        $staffmessages_sql = "SELECT COUNT(*) as count FROM staffmessages WHERE recipient_type = 'staff' AND status = 'unread'";
        $stmt = $conn->prepare($staffmessages_sql);
        if ($stmt) {
            $stmt->execute();
            $result = $stmt->get_result();
            $row = $result->fetch_assoc();
            $staffmessages_count = (int)$row['count'];
            $stmt->close();
        }
    }
    
    $total_unread_count = $messages_count + $staffmessages_count;
    
    echo json_encode([
        "status" => "success",
        "unread_count" => $total_unread_count,
        "messages_count" => $messages_count,
        "staffmessages_count" => $staffmessages_count,
        "debug" => [
            "user_type" => $user_type,
            "user_id" => $user_id,
            "total_unread" => $total_unread_count
        ]
    ]);
    
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid request method."
    ]);
}

$conn->close();
?>
