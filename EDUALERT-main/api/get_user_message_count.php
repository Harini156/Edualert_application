<?php
// Get User Message Count API - Get unread message count for specific user
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
        $user_type = trim($_POST['user_type'] ?? '');

        if (empty($user_id) || empty($user_type)) {
            $response['status'] = 'error';
            $response['message'] = 'User ID and user type are required.';
            echo json_encode($response);
            exit;
        }

        if (!in_array($user_type, ['student', 'staff'])) {
            $response['status'] = 'error';
            $response['message'] = 'Invalid user type. Must be "student" or "staff".';
            echo json_encode($response);
            exit;
        }

        // Get user details for filtering
        $user_details = [];
        if ($user_type === 'student') {
            $user_stmt = $conn->prepare("SELECT u.dept, sd.department, sd.year FROM users u LEFT JOIN student_details sd ON u.user_id = sd.user_id WHERE u.user_id = ?");
            $user_stmt->bind_param("s", $user_id);
            $user_stmt->execute();
            $user_result = $user_stmt->get_result();
            if ($user_result->num_rows > 0) {
                $user_data = $user_result->fetch_assoc();
                $user_details['department'] = $user_data['department'] ?? $user_data['dept'];
                $user_details['year'] = $user_data['year'];
            }
            $user_stmt->close();
        } elseif ($user_type === 'staff') {
            $user_stmt = $conn->prepare("SELECT u.dept, stf.department, stf.designation, stf.staff_type FROM users u LEFT JOIN staff_details stf ON u.user_id = stf.user_id WHERE u.user_id = ?");
            $user_stmt->bind_param("s", $user_id);
            $user_stmt->execute();
            $user_result = $user_stmt->get_result();
            if ($user_result->num_rows > 0) {
                $user_data = $user_result->fetch_assoc();
                $user_details['department'] = $user_data['department'] ?? $user_data['dept'];
                $user_details['designation'] = $user_data['designation'];
                $user_details['staff_type'] = $user_data['staff_type'];
            }
            $user_stmt->close();
        }

        $total_unread = 0;

        // Count messages from 'messages' table (admin messages)
        if ($user_type === 'student') {
            $admin_sql = "SELECT m.id FROM messages m 
                         WHERE (m.recipient_type = 'student' OR m.recipient_type = 'both')
                         AND (m.department IS NULL OR m.department = '' OR m.department = ?)
                         AND (m.year IS NULL OR m.year = '' OR m.year = ?)
                         AND m.id NOT IN (
                             SELECT ums.message_id FROM user_message_status ums 
                             WHERE ums.user_id = ? AND ums.message_table = 'messages' 
                             AND ums.status IN ('read', 'deleted')
                         )";
            $stmt = $conn->prepare($admin_sql);
            $stmt->bind_param("sss", $user_details['department'], $user_details['year'], $user_id);
        } else { // staff
            $admin_sql = "SELECT m.id FROM messages m 
                         WHERE (m.recipient_type = 'staff' OR m.recipient_type = 'both')
                         AND (m.department IS NULL OR m.department = '' OR m.department = ?)
                         AND (m.staff_type IS NULL OR m.staff_type = '' OR m.staff_type = ?)
                         AND (m.designation IS NULL OR m.designation = '' OR m.designation = ?)
                         AND m.id NOT IN (
                             SELECT ums.message_id FROM user_message_status ums 
                             WHERE ums.user_id = ? AND ums.message_table = 'messages' 
                             AND ums.status IN ('read', 'deleted')
                         )";
            $stmt = $conn->prepare($admin_sql);
            $stmt->bind_param("ssss", $user_details['department'], $user_details['staff_type'], $user_details['designation'], $user_id);
        }
        
        $stmt->execute();
        $result = $stmt->get_result();
        $admin_count = $result->num_rows;
        $stmt->close();

        // Count messages from 'staffmessages' table
        if ($user_type === 'student') {
            $staff_sql = "SELECT sm.id FROM staffmessages sm 
                         WHERE sm.recipient_type = 'student'
                         AND (sm.department IS NULL OR sm.department = '' OR sm.department = ?)
                         AND (sm.year IS NULL OR sm.year = '' OR sm.year = ?)
                         AND sm.id NOT IN (
                             SELECT ums.message_id FROM user_message_status ums 
                             WHERE ums.user_id = ? AND ums.message_table = 'staffmessages' 
                             AND ums.status IN ('read', 'deleted')
                         )";
            $stmt = $conn->prepare($staff_sql);
            $stmt->bind_param("sss", $user_details['department'], $user_details['year'], $user_id);
        } else { // staff
            $staff_sql = "SELECT sm.id FROM staffmessages sm 
                         WHERE sm.recipient_type = 'staff'
                         AND (sm.department IS NULL OR sm.department = '' OR sm.department = ?)
                         AND (sm.designation IS NULL OR sm.designation = '' OR sm.designation = ?)
                         AND sm.id NOT IN (
                             SELECT ums.message_id FROM user_message_status ums 
                             WHERE ums.user_id = ? AND ums.message_table = 'staffmessages' 
                             AND ums.status IN ('read', 'deleted')
                         )";
            $stmt = $conn->prepare($staff_sql);
            $stmt->bind_param("sss", $user_details['department'], $user_details['designation'], $user_id);
        }
        
        $stmt->execute();
        $result = $stmt->get_result();
        $staff_count = $result->num_rows;
        $stmt->close();

        $total_unread = $admin_count + $staff_count;

        $response['status'] = 'success';
        $response['unread_count'] = $total_unread;
        $response['admin_messages_count'] = $admin_count;
        $response['staff_messages_count'] = $staff_count;
        $response['user_details'] = $user_details;

    } else {
        $response['status'] = 'error';
        $response['message'] = 'Invalid request method. Expected POST.';
    }
} catch (Exception $e) {
    $response['status'] = 'error';
    $response['message'] = 'Server error occurred: ' . $e->getMessage();
}

if (isset($conn)) {
    $conn->close();
}

echo json_encode($response);
?>