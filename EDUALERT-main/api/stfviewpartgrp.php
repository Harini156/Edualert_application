<?php
header('Content-Type: application/json');
include 'db.php'; // DB connection

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

// Step 1: Get group_id or group_code
$group_id = null;

if (isset($_GET['group_id']) && !empty($_GET['group_id'])) {
    $group_id = intval($_GET['group_id']);
} elseif (isset($_GET['group_code']) && !empty($_GET['group_code'])) {
    $group_code = $_GET['group_code'];

    // Convert group_code to group_id
    $stmt = $conn->prepare("SELECT group_id FROM grpstaff WHERE group_code = ?");
    $stmt->bind_param("s", $group_code);
    $stmt->execute();
    $res = $stmt->get_result();

    if ($res->num_rows === 0) {
        echo json_encode([
            "status" => "error",
            "message" => "Invalid group_code"
        ]);
        exit;
    }

    $group_id = $res->fetch_assoc()['group_id'];
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Missing group_id or group_code"
    ]);
    exit;
}

// Step 2: Fetch student members of this group
$stmt = $conn->prepare("
    SELECT s.user_id, s.name, s.email, gm.joined_at
    FROM group_members gm
    JOIN students s ON gm.user_id = s.user_id
    WHERE gm.group_id = ?
");
$stmt->bind_param("i", $group_id);
$stmt->execute();
$result = $stmt->get_result();

$members = [];
while ($row = $result->fetch_assoc()) {
    $members[] = $row;
}

// Step 3: Output result
echo json_encode([
    "status" => "success",
    "total" => count($members),
    "members" => $members
]);
?>
