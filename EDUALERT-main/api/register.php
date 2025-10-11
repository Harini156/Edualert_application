<?php
header('Content-Type: application/json');
session_start();
include 'db.php'; // Ensure $conn is established

$response = [];

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $name      = trim($_POST['name']);
    $email     = trim($_POST['email']);
    $password  = $_POST['password'];
    $cpassword = $_POST['cpassword'];
    $usertype  = $_POST['usertype'];

    // For students only
    $department = isset($_POST['department']) ? trim($_POST['department']) : null;
    $year       = isset($_POST['year']) ? trim($_POST['year']) : null;

    $allowedTypes = ['admin', 'staff', 'student'];
    if (!in_array($usertype, $allowedTypes)) {
        echo json_encode(['status' => 'error', 'message' => 'Invalid usertype.']);
        exit;
    }

    if (empty($name) || empty($email) || empty($password) || empty($cpassword)) {
        echo json_encode(['status' => 'error', 'message' => 'All fields are required.']);
        exit;
    }

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        echo json_encode(['status' => 'error', 'message' => 'Invalid email format.']);
        exit;
    }

    if ($password !== $cpassword) {
        echo json_encode(['status' => 'error', 'message' => 'Passwords do not match.']);
        exit;
    }

    // Validate department and year if student
    if ($usertype === 'student' && (empty($department) || empty($year))) {
        echo json_encode(['status' => 'error', 'message' => 'Department and year are required for students.']);
        exit;
    }

    $tableName = $usertype . 's'; // students, staff, admins

    $checkStmt = $conn->prepare("SELECT id FROM $tableName WHERE email = ?");
    $checkStmt->bind_param("s", $email);
    $checkStmt->execute();
    $checkStmt->store_result();
    if ($checkStmt->num_rows > 0) {
        echo json_encode(['status' => 'error', 'message' => 'Email already exists.']);
        exit;
    }
    $checkStmt->close();

    // Generate user_id
    $prefixMap = ['student' => 'STU', 'staff' => 'STF', 'admin' => 'ADM'];
    $prefix = $prefixMap[$usertype];

    $latestStmt = $conn->prepare("SELECT user_id FROM $tableName ORDER BY id DESC LIMIT 1");
    $latestStmt->execute();
    $latestStmt->bind_result($lastUserId);
    $latestStmt->fetch();
    $latestStmt->close();

    $nextNumber = ($lastUserId && preg_match('/(\d+)$/', $lastUserId, $matches)) ? intval($matches[1]) + 1 : 1;
    $user_id = $prefix . str_pad($nextNumber, 3, '0', STR_PAD_LEFT);

    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);

    if ($usertype === 'student') {
        $stmt = $conn->prepare("INSERT INTO students (name, email, password, usertype, user_id, department, year) VALUES (?, ?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("sssssss", $name, $email, $hashedPassword, $usertype, $user_id, $department, $year);
    } else {
        $stmt = $conn->prepare("INSERT INTO $tableName (name, email, password, usertype, user_id) VALUES (?, ?, ?, ?, ?)");
        $stmt->bind_param("sssss", $name, $email, $hashedPassword, $usertype, $user_id);
    }

    if ($stmt->execute()) {
        $_SESSION['user_id']  = $user_id;
        $_SESSION['name']     = $name;
        $_SESSION['email']    = $email;
        $_SESSION['usertype'] = $usertype;

        // Build response
        $response = [
            'status'  => 'success',
            'message' => 'Registered successfully.',
            'user_id' => $user_id,
            'session' => $_SESSION
        ];

        // Add dept/year if student
        if ($usertype === 'student') {
            $response['department'] = $department;
            $response['year'] = $year;
        }

        echo json_encode($response);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Registration failed: ' . $stmt->error]);
    }

    $stmt->close();
    $conn->close();
} else {
    echo json_encode(['status' => 'error', 'message' => 'Invalid request method.']);
}
?>
