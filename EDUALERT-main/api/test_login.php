<?php
// Dedicated login test file for debugging
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$response = [
    'test_name' => 'Login Parameter Test',
    'timestamp' => date('Y-m-d H:i:s'),
    'method' => $_SERVER['REQUEST_METHOD']
];

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $response['post_data_received'] = $_POST;
    $response['post_data_count'] = count($_POST);
    
    // Check what parameters we received
    $response['parameters_analysis'] = [
        'email_param' => isset($_POST['email']) ? 'RECEIVED' : 'MISSING',
        'email_value' => $_POST['email'] ?? 'NOT_SENT',
        'email_length' => isset($_POST['email']) ? strlen($_POST['email']) : 0,
        
        'password_param' => isset($_POST['password']) ? 'RECEIVED' : 'MISSING', 
        'password_value' => isset($_POST['password']) ? (empty($_POST['password']) ? 'EMPTY' : 'PROVIDED') : 'NOT_SENT',
        'password_length' => isset($_POST['password']) ? strlen($_POST['password']) : 0,
        
        'role_param' => isset($_POST['role']) ? 'RECEIVED' : 'MISSING',
        'role_value' => $_POST['role'] ?? 'NOT_SENT',
        
        'login_id_param' => isset($_POST['login_id']) ? 'RECEIVED' : 'MISSING',
        'login_id_value' => $_POST['login_id'] ?? 'NOT_SENT'
    ];
    
    // Test the validation logic
    $email = trim($_POST['email'] ?? $_POST['login_id'] ?? '');
    $password = $_POST['password'] ?? '';
    
    $response['validation_test'] = [
        'processed_email' => $email,
        'processed_email_empty' => empty($email),
        'processed_password_empty' => empty($password),
        'would_fail_validation' => (empty($email) || empty($password))
    ];
    
    if (empty($email) || empty($password)) {
        $response['status'] = 'error';
        $response['message'] = 'Email/User ID and password are required.';
        $response['reason'] = 'Validation failed - empty fields detected';
    } else {
        $response['status'] = 'success';
        $response['message'] = 'Parameters received correctly!';
        $response['reason'] = 'All required fields have data';
        
        // Test database connection
        try {
            include 'db.php';
            $response['database_test'] = 'Connected successfully';
            
            // Test if user exists (without password check) - using correct 'users' table
            $stmt = $conn->prepare("SELECT user_id, name, email, user_type FROM users WHERE email = ? LIMIT 1");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $result = $stmt->get_result();
            
            if ($result->num_rows > 0) {
                $user = $result->fetch_assoc();
                $response['user_test'] = [
                    'user_found' => true,
                    'user_id' => $user['user_id'],
                    'name' => $user['name'],
                    'user_type' => $user['user_type'],
                    'table_used' => 'users (correct table)'
                ];
            } else {
                $response['user_test'] = [
                    'user_found' => false,
                    'message' => 'No user found with this email in users table',
                    'table_used' => 'users (correct table)'
                ];
            }
            $stmt->close();
            $conn->close();
            
        } catch (Exception $e) {
            $response['database_test'] = 'Failed: ' . $e->getMessage();
        }
    }
    
} else {
    $response['status'] = 'error';
    $response['message'] = 'Only POST method supported';
    $response['received_method'] = $_SERVER['REQUEST_METHOD'];
}

echo json_encode($response, JSON_PRETTY_PRINT);
?>