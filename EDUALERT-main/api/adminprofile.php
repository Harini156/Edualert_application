<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

include('db.php'); // Connects $conn to your database

$response = [];

// Support both JSON input and form input
$input = json_decode(file_get_contents("php://input"), true);
$admin_id = isset($input['admin_id']) ? trim($input['admin_id']) : (isset($_POST['admin_id']) ? trim($_POST['admin_id']) : '');

if (empty($admin_id)) {
    $response['status'] = 'error';
    $response['message'] = 'Admin ID is required.';
    echo json_encode($response);
    exit;
}

// Fetch admin details
$sql = "SELECT id, name, email, user_id, usertype FROM admins WHERE user_id = ? AND usertype = 'admin'";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $admin_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    $response['status'] = 'error';
    $response['message'] = 'Admin not found.';
} else {
    $admin = $result->fetch_assoc();
    $response['status'] = 'success';
    $response['admin'] = $admin;
}

echo json_encode($response);

$stmt->close();
$conn->close();
?>
