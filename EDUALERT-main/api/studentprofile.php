<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

include('db.php');

$response = [];

// Accepting input from both JSON and GET
$student_id = $_GET['student_id'] ?? '';

// Validate input
if (empty($student_id)) {
    echo json_encode([
        "status" => "error",
        "message" => "Student ID is required."
    ]);
    exit;
}

// Query to get student profile
$sql = "SELECT id, name, department, year, email, user_id, usertype FROM students WHERE user_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $student_id);
$stmt->execute();
$result = $stmt->get_result();

// Return student details
if ($result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Student not found."
    ]);
} else {
    $student = $result->fetch_assoc();
    echo json_encode([
        "status" => "success",
        "student" => $student
    ]);
}

$stmt->close();
$conn->close();
?>
