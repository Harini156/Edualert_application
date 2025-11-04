<?php
// Get Admin Messages for Students/Staff
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

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
            $response['status'] = false;
            $response['message'] = 'User ID is required.';
            echo json_encode($response);
            exit;
        }

        // Get user details to determine user type and filters
        $user_stmt = $conn->prepare("SELECT u.user_type, u.dept, sd.year, sd.department as student_dept, 
                                    stf.staff_type, stf.department as staff_dept, stf.designation 
                                    FROM users u 
                                    LEFT JOIN student_details sd ON u.user_id = sd.user_id 
                                    LEFT JOIN staff_details stf ON u.user_id = stf.user_id 
                                    WHERE u.user_id = ?");
        $user_stmt->bind_param("s", $user_id);
        $user_stmt->execute();
        $user_result = $user_stmt->get_result();
        
        if ($user_result->num_rows == 0) {
            $response['status'] = false;
            $response['message'] = 'User not found.';
            echo json_encode($response);
            exit;
        }
        
        $user_data = $user_result->fetch_assoc();
        $user_type = $user_data['user_type'];
        $user_stmt->close();

        $messages = [];

        if ($user_type == 'student') {
            // Get admin messages for students
            $department = $user_data['student_dept'] ?? $user_data['dept'];
            $year = $user_data['year'];
            
            $stmt = $conn->prepare("SELECT id, title, content, attachment, created_at, 'admin' as sender_type 
                                  FROM messages 
                                  WHERE (recipient_type = 'student' OR recipient_type = 'both') 
                                  AND (department IS NULL OR department = '' OR department = ?) 
                                  AND (year IS NULL OR year = '' OR year = ?) 
                                  ORDER BY created_at DESC");
            $stmt->bind_param("ss", $department, $year);
            
        } elseif ($user_type == 'staff') {
            // Get admin messages for staff
            $department = $user_data['staff_dept'] ?? $user_data['dept'];
            $staff_type = $user_data['staff_type'];
            $designation = $user_data['designation'];
            
            $stmt = $conn->prepare("SELECT id, title, content, attachment, created_at, 'admin' as sender_type 
                                  FROM messages 
                                  WHERE (recipient_type = 'staff' OR recipient_type = 'both') 
                                  AND (department IS NULL OR department = '' OR department = ?) 
                                  AND (staff_type IS NULL OR staff_type = '' OR staff_type = ?) 
                                  AND (designation IS NULL OR designation = '' OR designation = ?) 
                                  ORDER BY created_at DESC");
            $stmt->bind_param("sss", $department, $staff_type, $designation);
            
        } else {
            $response['status'] = false;
            $response['message'] = 'Invalid user type.';
            echo json_encode($response);
            exit;
        }

        $stmt->execute();
        $result = $stmt->get_result();
        
        while ($row = $result->fetch_assoc()) {
            $messages[] = [
                'id' => $row['id'],
                'title' => $row['title'],
                'content' => $row['content'],
                'attachment' => $row['attachment'],
                'created_at' => $row['created_at'],
                'sender_type' => 'admin'
            ];
        }
        $stmt->close();

        $response['status'] = true;
        $response['message'] = 'Messages retrieved successfully.';
        $response['messages'] = $messages;

    } else {
        $response['status'] = false;
        $response['message'] = 'Invalid request method.';
    }
} catch (Exception $e) {
    $response['status'] = false;
    $response['message'] = 'Server error occurred.';
}

if (isset($conn)) {
    $conn->close();
}

echo json_encode($response);
?>