<?php
// Staff details API - handles both GET (fetch) and POST (save) operations
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
        if (isset($_POST['dob']) || isset($_POST['staff_type']) || isset($_POST['phone'])) {
            // SAVE OPERATION - Save staff details
            $dob = trim($_POST['dob'] ?? '');
            $staff_type = trim($_POST['staff_type'] ?? '');
            $department = trim($_POST['department'] ?? null);
            $designation = trim($_POST['designation'] ?? null);
            $phone = trim($_POST['phone'] ?? '');
            $address = trim($_POST['address'] ?? '');

            // For non-teaching staff, department and designation should be null
            if (!empty($staff_type) && strtolower($staff_type) !== 'teaching') {
                $department = null;
                $designation = null;
            }

            // Check if staff details already exist
            $checkStmt = $conn->prepare("SELECT id FROM staff_details WHERE user_id = ?");
            $checkStmt->bind_param("s", $user_id);
            $checkStmt->execute();
            $checkResult = $checkStmt->get_result();
            $checkStmt->close();

            if ($checkResult->num_rows > 0) {
                // UPDATE existing record
                $stmt = $conn->prepare("UPDATE staff_details SET dob = ?, staff_type = ?, department = ?, designation = ?, phone = ?, address = ? WHERE user_id = ?");
                $stmt->bind_param("sssssss", $dob, $staff_type, $department, $designation, $phone, $address, $user_id);
            } else {
                // INSERT new record
                $stmt = $conn->prepare("INSERT INTO staff_details (user_id, dob, staff_type, department, designation, phone, address) VALUES (?, ?, ?, ?, ?, ?, ?)");
                $stmt->bind_param("sssssss", $user_id, $dob, $staff_type, $department, $designation, $phone, $address);
            }

            if ($stmt->execute()) {
                $response['status'] = 'success';
                $response['message'] = 'Staff details saved successfully.';
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Failed to save staff details: ' . $stmt->error;
            }
            $stmt->close();

        } else {
            // FETCH OPERATION - Get staff details
            $stmt = $conn->prepare("SELECT sd.*, u.name, u.email FROM staff_details sd 
                                   JOIN users u ON sd.user_id = u.user_id 
                                   WHERE sd.user_id = ?");
            $stmt->bind_param("s", $user_id);
            $stmt->execute();
            $result = $stmt->get_result();

            if ($result->num_rows > 0) {
                $staff = $result->fetch_assoc();
                
                $response['status'] = 'success';
                $response['message'] = 'Staff details retrieved successfully.';
                $response['data'] = [
                    'user_id' => $staff['user_id'],
                    'name' => $staff['name'],
                    'email' => $staff['email'],
                    'dob' => $staff['dob'],
                    'staff_type' => $staff['staff_type'],
                    'department' => $staff['department'],
                    'designation' => $staff['designation'],
                    'phone' => $staff['phone'],
                    'address' => $staff['address']
                ];
            } else {
                // If no details found in staff_details, create basic response from users table
                $stmt2 = $conn->prepare("SELECT user_id, name, email, user_type FROM users WHERE user_id = ?");
                $stmt2->bind_param("s", $user_id);
                $stmt2->execute();
                $result2 = $stmt2->get_result();
                
                if ($result2->num_rows > 0) {
                    $user = $result2->fetch_assoc();
                    $response['status'] = 'success';
                    $response['message'] = 'Basic staff info retrieved (no detailed profile found).';
                    $response['data'] = [
                        'user_id' => $user['user_id'],
                        'name' => $user['name'],
                        'email' => $user['email'],
                        'dob' => null,
                        'staff_type' => 'teaching', // default
                        'department' => null,
                        'designation' => null,
                        'phone' => null,
                        'address' => null
                    ];
                } else {
                    $response['status'] = 'error';
                    $response['message'] = 'Staff not found.';
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