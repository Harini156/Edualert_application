<?php
header('Content-Type: application/json');
include 'db.php';
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

// Accept JSON, POST, or GET input
$input = json_decode(file_get_contents("php://input"), true);
$student_id = $_GET['student_id'] ?? $_POST['student_id'] ?? ($input['student_id'] ?? '');

if (empty($student_id)) {
    echo json_encode(["status" => "error", "message" => "Student ID required"]);
    exit;
}

// Step 1: Get group_ids where this student is a member
$group_ids = [];
$stmt = $conn->prepare("SELECT group_id FROM group_members WHERE user_id = ?");
$stmt->bind_param("s", $student_id);
$stmt->execute();
$res = $stmt->get_result();

while ($row = $res->fetch_assoc()) {
    $group_ids[] = $row['group_id'];
}
$stmt->close();

// Step 2: Prepare placeholders if group_ids exist
$placeholders = '';
$params = [];

if (!empty($group_ids)) {
    $placeholders = implode(',', array_fill(0, count($group_ids), '?'));
    $params = $group_ids;
}

// Step 3: Fetch messages
$sql = "
    SELECT * FROM staffmessages
    WHERE 
        (receiver_id = ? AND is_group_message = 0) 
";

if (!empty($group_ids)) {
    $sql .= " OR (receiver_id IN ($placeholders) AND is_group_message = 1)";
}

$stmt = $conn->prepare($sql);

// Bind parameters
$types = str_repeat('s', 1 + count($params));
$stmt->bind_param($types, ...array_merge([$student_id], $params));
$stmt->execute();
$result = $stmt->get_result();

$messages = [];
while ($row = $result->fetch_assoc()) {
    $messages[] = $row;
}

echo json_encode([
    "status" => "success",
    "student_id" => $student_id,
    "messages" => $messages
]);
?>
