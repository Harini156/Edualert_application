<?php
header('Content-Type: application/json');
include 'db.php'; // Ensure this connects to your edualert DB

// Enable error reporting during development
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

// Step 1: Validate user_id in GET
if (!isset($_GET['user_id']) || empty($_GET['user_id'])) {
    echo json_encode([
        "status" => "error",
        "message" => "Missing user_id"
    ]);
    exit;
}

$user_id = $_GET['user_id'];

// Step 2: Check if the staff exists
$check = $conn->prepare("SELECT * FROM staffs WHERE user_id = ?");
$check->bind_param("s", $user_id);
$check->execute();
$result = $check->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid user_id"
    ]);
    exit;
}

// Step 3: Fetch all groups created by that user_id
$stmt = $conn->prepare("SELECT group_id, group_name, group_code, visibility, created_at FROM grpstaff WHERE created_by = ?");
$stmt->bind_param("s", $user_id);
$stmt->execute();
$groups_result = $stmt->get_result();

$groups = [];
while ($row = $groups_result->fetch_assoc()) {
    $groups[] = $row;
}

// Step 4: Return response
echo json_encode([
    "status" => "success",
    "total" => count($groups),
    "groups" => $groups
]);
?>
