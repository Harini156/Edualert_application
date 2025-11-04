<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

include 'db.php'; // Database connection

try {
    // Step 1: Validate sender_id (from GET or POST)
    $sender_id = '';
    if (isset($_GET['sender_id']) && !empty($_GET['sender_id'])) {
        $sender_id = trim($_GET['sender_id']);
    } elseif (isset($_POST['sender_id']) && !empty($_POST['sender_id'])) {
        $sender_id = trim($_POST['sender_id']);
    }

    if (empty($sender_id)) {
        echo json_encode([
            "status" => "error",
            "message" => "Missing sender_id"
        ]);
        exit;
    }

    // Step 2: Fetch messages sent by this staff using correct table structure
    $stmt = $conn->prepare("
        SELECT 
            id, 
            sender_id,
            title, 
            content, 
            recipient_type,
            department,
            year,
            designation,
            attachment, 
            created_at
        FROM staffmessages 
        WHERE sender_id = ?
        ORDER BY created_at DESC
    ");

    $stmt->bind_param("s", $sender_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $messages = [];
    while ($row = $result->fetch_assoc()) {
        $messages[] = [
            'id' => $row['id'],
            'sender_id' => $row['sender_id'],
            'title' => $row['title'],
            'content' => $row['content'],
            'recipient_type' => $row['recipient_type'],
            'department' => $row['department'],
            'year' => $row['year'],
            'designation' => $row['designation'],
            'attachment' => $row['attachment'],
            'created_at' => $row['created_at']
        ];
    }

    echo json_encode([
        "status" => "success",
        "total" => count($messages),
        "messages" => $messages
    ]);

    $stmt->close();
    
} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Database error occurred."
    ]);
}

$conn->close();
?>
