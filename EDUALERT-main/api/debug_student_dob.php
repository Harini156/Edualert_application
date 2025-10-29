<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

include('db.php');

// Check DOB data for all students
$sql = "SELECT u.user_id, u.name, u.email, sd.dob, sd.created_at 
        FROM users u 
        LEFT JOIN student_details sd ON u.user_id = sd.user_id 
        WHERE u.user_type = 'student' 
        ORDER BY u.user_id";

$result = $conn->query($sql);

$students = [];
while ($row = $result->fetch_assoc()) {
    $students[] = [
        'user_id' => $row['user_id'],
        'name' => $row['name'],
        'email' => $row['email'],
        'dob' => $row['dob'],
        'dob_is_null' => is_null($row['dob']),
        'created_at' => $row['created_at']
    ];
}

echo json_encode([
    'status' => 'success',
    'total_students' => count($students),
    'students' => $students
]);

$conn->close();
?>