<?php
header('Content-Type: application/json');
include 'db.php'; // Your DB connection

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

// Step 1: Get JSON input
$data = json_decode(file_get_contents("php://input"), true);

$student_id = $data['student_id'] ?? '';
$group_code = $data['group_code'] ?? '';

if (empty($student_id) || empty($group_code)) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing student_id or group_code"
    ]);
    exit;
}

// Step 2: Get group_id from group_code
$getGroup = $conn->prepare("SELECT group_id FROM grpstaff WHERE group_code = ?");
$getGroup->bind_param("s", $group_code);
$getGroup->execute();
$result = $getGroup->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid group code"
    ]);
    exit;
}

$group = $result->fetch_assoc();
$group_id = $group['group_id'];

// Step 3: Check if student is actually in the group
$check = $conn->prepare("SELECT * FROM group_members WHERE user_id = ? AND group_id = ?");
$check->bind_param("si", $student_id, $group_id);
$check->execute();
$check_result = $check->get_result();

if ($check_result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Student is not a member of this group"
    ]);
    exit;
}

// Step 4: Delete the record (leave group)
$delete = $conn->prepare("DELETE FROM group_members WHERE user_id = ? AND group_id = ?");
$delete->bind_param("si", $student_id, $group_id);

if ($delete->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Left the group successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Could not leave the group"
    ]);
}
?>
