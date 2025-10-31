<?php
// Simple GET-based login test for easy URL testing
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

$response = [
    'test_name' => 'Simple Login Test (GET-based)',
    'timestamp' => date('Y-m-d H:i:s'),
    'method' => $_SERVER['REQUEST_METHOD']
];

// Accept both GET and POST for easy testing
$email = $_GET['email'] ?? $_POST['email'] ?? 'rajarivijayalakshmi1157.sse@saveetha.com';
$password = $_GET['password'] ?? $_POST['password'] ?? 'test123';
$role = $_GET['role'] ?? $_POST['role'] ?? 'staff';

$response['test_parameters'] = [
    'email' => $email,
    'password_provided' => !empty($password),
    'role' => $role
];

// Test database connection and user lookup
try {
    include 'db.php';
    $response['database_connection'] = 'SUCCESS';
    
    // Test the exact same query as the fixed login.php
    $stmt = $conn->prepare("SELECT id, name, email, password, user_type, user_id FROM users WHERE email = ? OR user_id = ?");
    $stmt->bind_param("ss", $email, $email);
    $stmt->execute();
    $stmt->store_result();
    
    if ($stmt->num_rows > 0) {
        $stmt->bind_result($id, $name, $db_email, $hashedPassword, $user_type, $user_id);
        $stmt->fetch();
        
        $response['user_lookup'] = [
            'status' => 'USER_FOUND',
            'user_id' => $user_id,
            'name' => $name,
            'email' => $db_email,
            'user_type' => $user_type,
            'password_hash_exists' => !empty($hashedPassword)
        ];
        
        // Test password verification (if password provided)
        if (!empty($password)) {
            $password_valid = password_verify($password, $hashedPassword);
            $response['password_test'] = [
                'password_valid' => $password_valid,
                'hash_length' => strlen($hashedPassword),
                'test_password' => $password
            ];
        }
        
    } else {
        $response['user_lookup'] = [
            'status' => 'USER_NOT_FOUND',
            'searched_email' => $email,
            'table_used' => 'users'
        ];
    }
    
    $stmt->close();
    $conn->close();
    
    $response['overall_status'] = 'SUCCESS - Database and table structure working correctly';
    
} catch (Exception $e) {
    $response['database_connection'] = 'FAILED';
    $response['error'] = $e->getMessage();
    $response['overall_status'] = 'FAILED - Database issue: ' . $e->getMessage();
}

echo json_encode($response, JSON_PRETTY_PRINT);
?>