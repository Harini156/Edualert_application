<?php
header('Content-Type: application/json');
include 'db.php'; // DB connection

// Enable error reporting (for debugging)
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Step 1: Get user_id from GET
if (!isset($_GET['user_id']) || empty($_GET['user_id'])) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing user_id"
    ]);
    exit;
}

$user_id = trim($_GET['user_id']); // remove extra spaces

// Step 2: Check if student exists in students table
$check = $conn->prepare("SELECT * FROM students WHERE user_id = ?");
$check->bind_param("s", $user_id);
$check->execute();
$check_result = $check->get_result();

if ($check_result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid student ID"
    ]);
    exit;
}

// Step 3: Fetch groups joined by this student
$stmt = $conn->prepare("
    SELECT g.group_id, g.group_name, g.group_code, g.visibility, gm.joined_at
    FROM group_members gm
    JOIN grpstaff g ON gm.group_id = g.group_id
    WHERE gm.user_id = ?
");
$stmt->bind_param("s", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$groups = [];
while ($row = $result->fetch_assoc()) {
    $groups[] = $row;
}

// Step 4: Return result
echo json_encode([
    "status" => "success",
    "total" => count($groups),
    "groups" => $groups
]);
?>
