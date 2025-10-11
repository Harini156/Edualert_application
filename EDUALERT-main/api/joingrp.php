<?php
header('Content-Type: application/json');
include 'db.php';

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

// Step 1: Get input
$data = json_decode(file_get_contents("php://input"), true);
$student_id = $data['student_id'] ?? '';
$group_code = $data['group_code'] ?? '';
$group_name = $data['group_name'] ?? ''; // For public groups

// Step 2: Validate student ID
if (empty($student_id)) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing student ID"
    ]);
    exit;
}

// Step 3: Check if student exists
$checkStudent = $conn->prepare("SELECT * FROM students WHERE user_id = ?");
$checkStudent->bind_param("s", $student_id);
$checkStudent->execute();
$studentResult = $checkStudent->get_result();

if ($studentResult->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid student ID"
    ]);
    exit;
}

// Step 4: Determine which group to fetch
if (!empty($group_code)) {
    // Private group join — use group_code
    $group_stmt = $conn->prepare("SELECT group_id, visibility FROM grpstaff WHERE group_code = ?");
    $group_stmt->bind_param("s", $group_code);
} elseif (!empty($group_name)) {
    // Public group join — use group_name
    $group_stmt = $conn->prepare("SELECT group_id, visibility FROM grpstaff WHERE group_name = ? AND visibility = 'public'");
    $group_stmt->bind_param("s", $group_name);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Provide group_code (for private) or group_name (for public)"
    ]);
    exit;
}

$group_stmt->execute();
$group_result = $group_stmt->get_result();

if ($group_result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Group not found or access denied"
    ]);
    exit;
}

$group = $group_result->fetch_assoc();
$group_id = $group['group_id'];
$visibility = $group['visibility'];

// Step 5: Check if student already joined
$check_stmt = $conn->prepare("SELECT * FROM group_members WHERE user_id = ? AND group_id = ?");
$check_stmt->bind_param("si", $student_id, $group_id);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows > 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Already joined this group"
    ]);
    exit;
}

// Step 6: Join the group
$insert_stmt = $conn->prepare("INSERT INTO group_members (user_id, group_id, joined_at) VALUES (?, ?, NOW())");
$insert_stmt->bind_param("si", $student_id, $group_id);

if ($insert_stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Student joined the group successfully",
        "group_visibility" => $visibility
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to join group"
    ]);
}
?>
