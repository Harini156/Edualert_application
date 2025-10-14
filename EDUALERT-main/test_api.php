<?php
// Test file to help you find the correct server URL
// Save this as test_api.php in your server root directory

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

echo json_encode([
    "status" => "success",
    "message" => "API is working!",
    "server_info" => [
        "php_version" => phpversion(),
        "current_time" => date('Y-m-d H:i:s'),
        "server_ip" => $_SERVER['SERVER_ADDR'] ?? 'unknown',
        "request_method" => $_SERVER['REQUEST_METHOD'],
        "user_agent" => $_SERVER['HTTP_USER_AGENT'] ?? 'unknown'
    ]
]);
?>
