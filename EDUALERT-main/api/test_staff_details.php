<?php
// Test staff_details.php functionality
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json');

// Test with known staff user_id from your test results
$test_user_id = 'STF014'; // From your successful login test

echo "Testing staff_details.php with user_id: $test_user_id\n\n";

// Simulate POST request
$_POST['user_id'] = $test_user_id;
$_SERVER['REQUEST_METHOD'] = 'POST';

// Include the actual staff_details.php
include 'staff_details.php';
?>