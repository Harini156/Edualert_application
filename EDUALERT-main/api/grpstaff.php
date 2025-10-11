<?php
header('Content-Type: application/json');
include 'db.php'; // Ensure this connects to your edualert DB

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

// Step 1: Get raw JSON input
$data = json_decode(file_get_contents("php://input"), true);

$group_name = $data['group_name'] ?? '';
$created_by = $data['created_by'] ?? '';
$visibility = $data['visibility'] ?? 'private';

// Step 2: Validate required fields
if (empty($group_name) || empty($created_by)) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing required fields"
    ]);
    exit;
}

// Step 3: Validate staff exists
$check = $conn->prepare("SELECT * FROM staffs WHERE user_id = ?");
$check->bind_param("s", $created_by);
$check->execute();
$result = $check->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid staff ID"
    ]);
    exit;
}

// ✅ Step 4: Check if group with same name already exists for this staff
$dup_check = $conn->prepare("SELECT * FROM grpstaff WHERE group_name = ? AND created_by = ?");
$dup_check->bind_param("ss", $group_name, $created_by);
$dup_check->execute();
$dup_result = $dup_check->get_result();

if ($dup_result->num_rows > 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Group with this name already exists for this staff"
    ]);
    exit;
}

// Step 5: Insert group without group_code
$stmt = $conn->prepare("INSERT INTO grpstaff (group_name, created_by, visibility) VALUES (?, ?, ?)");
$stmt->bind_param("sss", $group_name, $created_by, $visibility);

if ($stmt->execute()) {
    $last_id = $conn->insert_id;
    $year = date("Y");
    $group_code = "GRP" . $year . "_" . str_pad($last_id, 3, "0", STR_PAD_LEFT);

    // Step 6: Update group_code
    $update = $conn->prepare("UPDATE grpstaff SET group_code = ? WHERE group_id = ?");
    $update->bind_param("si", $group_code, $last_id);
    $update->execute();

    echo json_encode([
        "status" => "success",
        "message" => "Group created successfully",
        "group_code" => $group_code
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Group creation failed"
    ]);
}
?>
