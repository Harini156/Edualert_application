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

    // Check if email already exists in users table
    $checkStmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
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

    $latestStmt = $conn->prepare("SELECT user_id FROM users WHERE user_type = ? ORDER BY id DESC LIMIT 1");
    $latestStmt->bind_param("s", $usertype);
    $latestStmt->execute();
    $latestStmt->bind_result($lastUserId);
    $latestStmt->fetch();
    $latestStmt->close();

    $nextNumber = ($lastUserId && preg_match('/(\d+)$/', $lastUserId, $matches)) ? intval($matches[1]) + 1 : 1;
    $user_id = $prefix . str_pad($nextNumber, 3, '0', STR_PAD_LEFT);

    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);

    // Insert into users table only (no department/year here)
    $stmt = $conn->prepare("INSERT INTO users (name, email, password, user_type, user_id, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
    $stmt->bind_param("sssss", $name, $email, $hashedPassword, $usertype, $user_id);

    if ($stmt->execute()) {
        // Create placeholder entries in detail tables to prevent null department issues
        if ($usertype === 'student') {
            // Create placeholder student_details entry
            $studentStmt = $conn->prepare("INSERT INTO student_details (user_id, department, year, stay_type, backlogs) VALUES (?, '', 1, 'Day Scholar', 0)");
            $studentStmt->bind_param("s", $user_id);
            $studentStmt->execute();
            $studentStmt->close();
        } elseif ($usertype === 'staff') {
            // Create placeholder staff_details entry
            $staffStmt = $conn->prepare("INSERT INTO staff_details (user_id, staff_type) VALUES (?, 'teaching')");
            $staffStmt->bind_param("s", $user_id);
            $staffStmt->execute();
            $staffStmt->close();
        }

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
