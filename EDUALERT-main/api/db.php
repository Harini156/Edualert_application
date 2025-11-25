<?php
// db.php - JSON-safe database connection

// Suppress all PHP errors to prevent JSON corruption
error_reporting(0);
ini_set('display_errors', 0);

$host = "localhost";     // Database host
$user = "root";          // Database username
$pass = "";              // Database password
$dbname = "edualert";    // Database name

// Create connection
$conn = new mysqli($host, $user, $pass, $dbname);

// Check connection - JSON-safe error handling
if ($conn->connect_error) {
    header('Content-Type: application/json');
    $response = [
        'status' => 'error',
        'message' => 'Database connection failed. Please contact administrator.',
        'debug' => 'Connection error: ' . $conn->connect_error
    ];
    echo json_encode($response);
    exit();
}

// Set charset to prevent encoding issues
$conn->set_charset("utf8");

// CRITICAL FIX: Set timezone to India (IST) for all database operations
// This ensures all timestamps are in Indian Standard Time
$conn->query("SET time_zone = '+05:30'");

// Set PHP timezone to match
date_default_timezone_set('Asia/Kolkata');
?>
