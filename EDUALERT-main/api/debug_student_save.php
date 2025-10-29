<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

// Debug API to see exactly what data is being received
$debug_info = [
    'request_method' => $_SERVER['REQUEST_METHOD'],
    'content_type' => $_SERVER['CONTENT_TYPE'] ?? 'not set',
    'post_data' => $_POST,
    'raw_input' => file_get_contents("php://input"),
    'all_headers' => getallheaders(),
    'timestamp' => date('Y-m-d H:i:s')
];

// Specifically check DOB field
$dob_received = isset($_POST['dob']) ? $_POST['dob'] : 'NOT_SET';
$debug_info['dob_analysis'] = [
    'dob_value' => $dob_received,
    'dob_length' => strlen($dob_received),
    'dob_is_empty' => empty($dob_received),
    'dob_isset' => isset($_POST['dob'])
];

echo json_encode($debug_info, JSON_PRETTY_PRINT);
?>