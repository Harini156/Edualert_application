<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

include('db.php'); // adjust path if needed

$response = [];

// No need for student_id here since admin sends to all students
$sql = "
    SELECT id, sender_id, recipient_type, subject, message, attachment, sent_at 
    FROM messages 
    WHERE recipient_type = 'student'
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
