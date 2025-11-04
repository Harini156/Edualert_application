<?php
// Simple endpoint test
header('Content-Type: application/json');

echo "Testing API Endpoints:\n\n";

// Test 1: getadminmsg.php
echo "1. Testing getadminmsg.php:\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/getadminmsg.php");
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query(['user_id' => 'STU001']));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_TIMEOUT, 5);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);
echo "HTTP Code: $httpCode\n";
echo "Response: " . substr($response, 0, 200) . "...\n\n";

// Test 2: get_student_messages.php
echo "2. Testing get_student_messages.php:\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/get_student_messages.php");
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query(['user_id' => 'STU001']));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_TIMEOUT, 5);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);
echo "HTTP Code: $httpCode\n";
echo "Response: " . substr($response, 0, 200) . "...\n\n";

// Test 3: get_staff_messages.php
echo "3. Testing get_staff_messages.php:\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/get_staff_messages.php");
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query(['user_id' => 'STF001']));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_TIMEOUT, 5);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);
echo "HTTP Code: $httpCode\n";
echo "Response: " . substr($response, 0, 200) . "...\n\n";

echo "Test completed!";
?>