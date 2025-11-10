<?php
// Student details API - handles both GET (fetch) and POST (save) operations
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

        // Check if this is a save operation (has additional fields)
        if (isset($_POST['dob']) || isset($_POST['gender']) || isset($_POST['department'])) {
            // SAVE OPERATION - Save student details
            $dob = trim($_POST['dob'] ?? '');
            $gender = trim($_POST['gender'] ?? '');
            $blood_group = trim($_POST['blood_group'] ?? '');
            $department = trim($_POST['department'] ?? '');
            $year = trim($_POST['year'] ?? '');
            $cgpa = trim($_POST['cgpa'] ?? '');
            $backlogs = trim($_POST['backlogs'] ?? '0');
            $stay_type = trim($_POST['stay_type'] ?? '');
            $phone = trim($_POST['phone'] ?? '');
            $address = trim($_POST['address'] ?? '');

            // Convert year from display format to number (handle both Roman and numeric formats)
            $yearNumber = 1; // default
            if (is_numeric($year)) {
                // If already numeric (1, 2, 3, 4), use it directly
                $yearNumber = intval($year);
                if ($yearNumber < 1 || $yearNumber > 4) $yearNumber = 1; // validate range
            } else {
                // If Roman format (I Year, II Year, etc.), convert it
                if (strpos($year, 'I Year') !== false) $yearNumber = 1;
                elseif (strpos($year, 'II Year') !== false) $yearNumber = 2;
                elseif (strpos($year, 'III Year') !== false) $yearNumber = 3;
                elseif (strpos($year, 'IV Year') !== false) $yearNumber = 4;
            }

            // Check if student details already exist
            $checkStmt = $conn->prepare("SELECT id FROM student_details WHERE user_id = ?");
            $checkStmt->bind_param("s", $user_id);
            $checkStmt->execute();
            $checkResult = $checkStmt->get_result();
            $checkStmt->close();

            if ($checkResult->num_rows > 0) {
                // UPDATE existing record
                $stmt = $conn->prepare("UPDATE student_details SET dob = ?, gender = ?, blood_group = ?, department = ?, year = ?, cgpa = ?, backlogs = ?, stay_type = ?, phone = ?, address = ? WHERE user_id = ?");
                $stmt->bind_param("ssssissssss", $dob, $gender, $blood_group, $department, $yearNumber, $cgpa, $backlogs, $stay_type, $phone, $address, $user_id);
            } else {
                // INSERT new record
                $stmt = $conn->prepare("INSERT INTO student_details (user_id, dob, gender, blood_group, department, year, cgpa, backlogs, stay_type, phone, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                $stmt->bind_param("ssssissssss", $user_id, $dob, $gender, $blood_group, $department, $yearNumber, $cgpa, $backlogs, $stay_type, $phone, $address);
            }

            if ($stmt->execute()) {
                $response['status'] = 'success';
                $response['message'] = 'Student details saved successfully.';
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Failed to save student details: ' . $stmt->error;
            }
            $stmt->close();

        } else {
            // FETCH OPERATION - Get student details
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
                $stmt2 = $conn->prepare("SELECT user_id, name, email, user_type FROM users WHERE user_id = ?");
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
                        'department' => null,
                        'year' => "1",
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
        }
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