<?php
// Suppress all PHP errors to prevent JSON corruption
error_reporting(0);
ini_set('display_errors', 0);

// Start output buffering to catch any unexpected output
ob_start();

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

session_start();

// Clear any previous output
ob_clean();

include 'db.php'; // Ensure this connects to your MySQL database

$response = [];

try {

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Android app sends 'email', 'password', and 'role'
    $login_id = trim($_POST['email'] ?? $_POST['login_id'] ?? ''); // Accept both email and login_id
    $password = $_POST['password'] ?? '';
    $role = $_POST['role'] ?? ''; // Role from Android app

    if (empty($login_id) || empty($password)) {
        $response['status'] = 'error';
        $response['message'] = 'Email/User ID and password are required.';
        $response['debug'] = [
            'received_email' => $_POST['email'] ?? 'not_sent',
            'received_login_id' => $_POST['login_id'] ?? 'not_sent', 
            'received_password' => empty($_POST['password']) ? 'empty' : 'provided',
            'received_role' => $_POST['role'] ?? 'not_sent',
            'all_post_data' => array_keys($_POST)
        ];
        echo json_encode($response);
        exit;
    }

    // Query the single 'users' table (not separate tables)
    $stmt = $conn->prepare("SELECT id, name, email, password, user_type, user_id FROM users WHERE email = ? OR user_id = ?");
    $stmt->bind_param("ss", $login_id, $login_id);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        $stmt->bind_result($id, $name, $email, $hashedPassword, $user_type, $user_id);
        $stmt->fetch();

        if (password_verify($password, $hashedPassword)) {
            $_SESSION['user_id']  = $user_id;
            $_SESSION['name']     = $name;
            $_SESSION['email']    = $email;
            $_SESSION['usertype'] = $user_type;

            $response['status']  = 'success';
            $response['message'] = 'Login successful.';
            $response['user'] = [
                'user_id'  => $user_id,
                'email'    => $email,
                'name'     => $name,
                'user_type' => $user_type
            ];
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Incorrect password.';
        }
    } else {
        $response['status'] = 'error';
        $response['message'] = 'No user found with this Email or User ID.';
    }
    
    $stmt->close();

    $conn->close();
} else {
    $response['status'] = 'error';
    $response['message'] = 'Invalid request method.';
}

} catch (Exception $e) {
    $response['status'] = 'error';
    $response['message'] = 'Server error occurred.';
    $response['debug'] = $e->getMessage();
}

// Clear any buffered output and send clean JSON
ob_clean();
echo json_encode($response);
ob_end_flush();
?>
