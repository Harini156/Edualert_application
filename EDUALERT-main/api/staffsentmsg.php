<?php
header('Content-Type: application/json');
include 'db.php'; // Database connection

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

// Step 1: Validate staff_id
if (!isset($_GET['staff_id']) || empty($_GET['staff_id'])) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing staff_id"
    ]);
    exit;
}

$staff_id = $_GET['staff_id'];

// Step 2: Fetch messages sent by this staff
$stmt = $conn->prepare("
    SELECT 
        m.id, 
        m.receiver_id, 
        m.title, 
        m.message, 
        m.attachment, 
        m.is_group_message, 
        m.sent_at,
        CASE 
            WHEN m.is_group_message = 1 THEN g.group_name 
            ELSE s.name 
        END AS receiver_name
    FROM staffmessages m
    LEFT JOIN students s ON m.receiver_id = s.user_id AND m.is_group_message = 0
    LEFT JOIN grpstaff g ON m.receiver_id = g.group_id AND m.is_group_message = 1
    WHERE m.sender_id = ?
    ORDER BY m.sent_at DESC
");

$stmt->bind_param("s", $staff_id);
$stmt->execute();
$result = $stmt->get_result();

$messages = [];
while ($row = $result->fetch_assoc()) {
    $messages[] = $row;
}

echo json_encode([
    "status" => "success",
    "total" => count($messages),
    "messages" => $messages
]);
