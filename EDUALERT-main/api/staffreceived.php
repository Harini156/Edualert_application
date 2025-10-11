<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

include('db.php');  // Adjust path as needed

$response = [];

$staff_id = isset($_POST['staff_id']) ? trim($_POST['staff_id']) : '';

if (empty($staff_id)) {
    echo json_encode([
        "status" => "error",
        "message" => "Staff ID is required."
    ]);
    exit;
}

// Since messages are sent to all staff with recipient_type='staff', 
// we just get those messages, no filtering by staff_id needed

$sql = "
    SELECT id, sender_id, recipient_type, subject, message, attachment, sent_at 
    FROM messages 
    WHERE recipient_type = 'staff'
    ORDER BY sent_at DESC
";

$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode([
        "status" => "error",
        "message" => "Database error: " . $conn->error
    ]);
    exit;
}

$stmt->execute();
$result = $stmt->get_result();

$messages = [];

while ($row = $result->fetch_assoc()) {
    $messages[] = $row;
}

echo json_encode([
    "status" => "success",
    "messages" => $messages
]);

$stmt->close();
$conn->close();
?>
