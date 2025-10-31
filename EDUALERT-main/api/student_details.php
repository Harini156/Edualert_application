<?php
// Student details API - fetches student profile information
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

include 'db.php';

$response = [];

try {
    if ($_SERVER["REQUEST_METHOD"] == "POST") {
        $user_id = trim($_POST['user_id'] ?? '');

        if (empty($user_id)) {
            $response['status'] = 'error';
            $response['message'] = 'User ID is required.';
            echo json_encode($response);
            exit;
        }

        // Get student details from student_details table
        $stmt = $conn->prepare("SELECT sd.*, u.name, u.email FROM student_details sd 
                               JOIN users u ON sd.user_id = u.user_id 
                               WHERE sd.user_id = ?");
        $stmt->bind_param("s", $user_id);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows > 0) {
            $student = $result->fetch_assoc();
            
            $response['status'] = 'success';
            $response['message'] = 'Student details retrieved successfully.';
            $response['data'] = [
                'user_id' => $student['user_id'],
                'name' => $student['name'],
                'email' => $student['email'],
                'department' => $student['department'],
                'year' => (string)$student['year'], // Convert to string as expected by Android
                'gender' => $student['gender'],
                'cgpa' => $student['cgpa'] ? (string)$student['cgpa'] : null,
                'stay_type' => $student['stay_type'],
                'dob' => $student['dob'],
                'blood_group' => $student['blood_group'],
                'phone' => $student['phone'],
                'address' => $student['address'],
                'backlogs' => $student['backlogs'] ? (string)$student['backlogs'] : "0"
            ];
        } else {
            // If no details found in student_details, create basic response from users table
            $stmt2 = $conn->prepare("SELECT user_id, name, email, user_type, dept, year FROM users WHERE user_id = ?");
            $stmt2->bind_param("s", $user_id);
            $stmt2->execute();
            $result2 = $stmt2->get_result();
            
            if ($result2->num_rows > 0) {
                $user = $result2->fetch_assoc();
                $response['status'] = 'success';
                $response['message'] = 'Basic student info retrieved (no detailed profile found).';
                $response['data'] = [
                    'user_id' => $user['user_id'],
                    'name' => $user['name'],
                    'email' => $user['email'],
                    'department' => $user['dept'],
                    'year' => $user['year'] ? (string)$user['year'] : "1",
                    'gender' => null,
                    'cgpa' => null,
                    'stay_type' => 'Day Scholar', // default
                    'dob' => null,
                    'blood_group' => null,
                    'phone' => null,
                    'address' => null,
                    'backlogs' => "0"
                ];
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Student not found.';
            }
            $stmt2->close();
        }
        
        $stmt->close();
    } else {
        $response['status'] = 'error';
        $response['message'] = 'Invalid request method.';
    }
} catch (Exception $e) {
    $response['status'] = 'error';
    $response['message'] = 'Server error occurred.';
    $response['debug'] = $e->getMessage();
}

if (isset($conn)) {
    $conn->close();
}

echo json_encode($response);
?>