<?php
header('Content-Type: application/json');
include 'db.php'; // Ensure DB connection is included

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

// Step 1: Get JSON input
$data = json_decode(file_get_contents("php://input"), true);

$staff_id = $data['staff_id'] ?? '';
$group_id = $data['group_id'] ?? '';

if (empty($staff_id) || empty($group_id)) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing staff_id or group_id"
    ]);
    exit;
}

// Step 2: Check if group exists and belongs to this staff
$check = $conn->prepare("SELECT * FROM grpstaff WHERE group_id = ? AND created_by = ?");
$check->bind_param("is", $group_id, $staff_id);
$check->execute();
$result = $check->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Group not found or does not belong to this staff"
    ]);
    exit;
}

// Step 3: Delete all members from group_members
$del_members = $conn->prepare("DELETE FROM group_members WHERE group_id = ?");
$del_members->bind_param("i", $group_id);
$del_members->execute();

// Step 4: Delete group from grpstaff
$del_group = $conn->prepare("DELETE FROM grpstaff WHERE group_id = ?");
$del_group->bind_param("i", $group_id);

if ($del_group->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Group deleted successfully"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to delete group"
    ]);
}
?>
