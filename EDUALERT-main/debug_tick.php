<?php
// Debug API to test tick button functionality
// Save this as debug_tick.php in your server root

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

// Log all requests
error_log("=== DEBUG TICK API CALLED ===");
error_log("Request Method: " . $_SERVER['REQUEST_METHOD']);
error_log("POST Data: " . print_r($_POST, true));
error_log("GET Data: " . print_r($_GET, true));

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $message_id = isset($_POST['message_id']) ? $_POST['message_id'] : 'NOT_SET';
    $table_name = isset($_POST['table_name']) ? $_POST['table_name'] : 'NOT_SET';
    
    echo json_encode([
        "status" => "success",
        "message" => "Debug API working!",
        "received_data" => [
            "message_id" => $message_id,
            "table_name" => $table_name,
            "message_id_type" => gettype($message_id),
            "table_name_type" => gettype($table_name)
        ],
        "raw_post" => $_POST,
        "server_info" => [
            "php_version" => phpversion(),
            "current_time" => date('Y-m-d H:i:s'),
            "request_method" => $_SERVER['REQUEST_METHOD']
        ]
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Please use POST method",
        "received_method" => $_SERVER['REQUEST_METHOD']
    ]);
}
?>
