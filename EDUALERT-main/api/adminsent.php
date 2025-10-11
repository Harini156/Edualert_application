<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

include('db.php'); // DB connection

// Get admin_id from POST request
$admin_id = isset($_POST['admin_id']) ? trim($_POST['admin_id']) : '';

if (empty($admin_id)) {
    echo json_encode(["status" => "error", "message" => "Admin ID is required"]);
    exit;
}

$sql = "SELECT id, sender_id, recipient_type, subject, message, attachment, sent_at FROM messages WHERE sender_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $admin_id);
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
