<?php
// Simple server test file
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

$response = [
    'status' => 'success',
    'message' => 'Server is working correctly!',
    'timestamp' => date('Y-m-d H:i:s'),
    'php_version' => phpversion(),
    'server_info' => $_SERVER['SERVER_SOFTWARE'] ?? 'Unknown'
];

// Test database connection
try {
    include 'db.php';
    $response['database'] = 'Connected successfully';
    $response['database_name'] = 'edualert';
} catch (Exception $e) {
    $response['database'] = 'Connection failed: ' . $e->getMessage();
}

echo json_encode($response, JSON_PRETTY_PRINT);
?>