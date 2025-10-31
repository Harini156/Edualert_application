<?php
// Test student_details.php functionality
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json');

// Test with known student user_id from your test results
$test_user_id = 'STU006'; // From your successful login test

echo "Testing student_details.php with user_id: $test_user_id\n\n";

// Simulate POST request
$_POST['user_id'] = $test_user_id;
$_SERVER['REQUEST_METHOD'] = 'POST';

// Include the actual student_details.php
include 'student_details.php';
?>