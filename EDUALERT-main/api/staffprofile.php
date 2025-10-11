<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

include('db.php'); // Connects $conn to your database

$response = [];

// Accept JSON or form input
$input = json_decode(file_get_contents("php://input"), true);
$staff_id = isset($input['staff_id']) ? trim($input['staff_id']) : (isset($_POST['staff_id']) ? trim($_POST['staff_id']) : '');

if (empty($staff_id)) {
    $response['status'] = 'error';
    $response['message'] = 'Staff ID is required.';
    echo json_encode($response);
    exit;
}

// Fetch staff details
$sql = "SELECT id, name, email, user_id, usertype FROM staffs WHERE user_id = ? AND usertype = 'staff'";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $staff_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    $response['status'] = 'error';
    $response['message'] = 'Staff not found.';
} else {
    $staff = $result->fetch_assoc();
    $response['status'] = 'success';
    $response['staff'] = $staff;
}

echo json_encode($response);

$stmt->close();
$conn->close();
?>
