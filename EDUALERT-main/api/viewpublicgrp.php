<?php
header('Content-Type: application/json');
include 'db.php'; // Connect to your edualert DB

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

// Step 1: Get all public groups
$stmt = $conn->prepare("SELECT group_id, group_name, group_code, created_by, created_at FROM grpstaff WHERE visibility = 'public'");
$stmt->execute();
$result = $stmt->get_result();

$groups = [];
while ($row = $result->fetch_assoc()) {
    $groups[] = $row;
}

// Step 2: Return the list
echo json_encode([
    "status" => "success",
    "total" => count($groups),
    "groups" => $groups
]);
?>
