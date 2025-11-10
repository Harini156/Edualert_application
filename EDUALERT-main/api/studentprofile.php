<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

include('db.php');

$response = [];

// Support both JSON input and form input
$input = json_decode(file_get_contents("php://input"), true);
$student_id = isset($input['student_id']) ? trim($input['student_id']) : (isset($_POST['student_id']) ? trim($_POST['student_id']) : (isset($_GET['student_id']) ? trim($_GET['student_id']) : ''));

// Validate input
if (empty($student_id)) {
    echo json_encode([
        "status" => "error",
        "message" => "Student ID is required."
    ]);
    exit;
}

// Query to get student profile from users table and join with student_details
// Get department and year from student_details table (not users table)
$sql = "SELECT u.id, u.name, u.email, u.user_id, u.user_type, 
               sd.department, sd.year, sd.dob, sd.gender, sd.blood_group, sd.cgpa, sd.backlogs, sd.stay_type, sd.phone, sd.address
        FROM users u 
        LEFT JOIN student_details sd ON u.user_id = sd.user_id 
        WHERE u.user_id = ? AND u.user_type = 'student'";
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
