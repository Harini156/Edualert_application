<?php
// Get Student Messages - For students to receive ONLY STAFF messages (no admin messages)
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

include 'db.php';

$response = [];

try {
    if ($_SERVER["REQUEST_METHOD"] == "POST" || $_SERVER["REQUEST_METHOD"] == "GET") {
        $user_id = trim($_POST['user_id'] ?? $_GET['user_id'] ?? '');

        if (empty($user_id)) {
            $response['status'] = false;
            $response['message'] = 'User ID is required.';
            echo json_encode($response);
            exit;
        }

        // Get student details to determine department and year
        $student_stmt = $conn->prepare("SELECT u.dept, sd.department, sd.year 
                                      FROM users u 
                                      LEFT JOIN student_details sd ON u.user_id = sd.user_id 
                                      WHERE u.user_id = ? AND u.user_type = 'student'");
        $student_stmt->bind_param("s", $user_id);
        $student_stmt->execute();
        $student_result = $student_stmt->get_result();
        
        if ($student_result->num_rows == 0) {
            $response['status'] = false;
            $response['message'] = 'Student not found.';
            echo json_encode($response);
            exit;
        }
        
        $student_data = $student_result->fetch_assoc();
        $department = $student_data['department'] ?? $student_data['dept'];
        $year = $student_data['year'];
        $student_stmt->close();

        $messages = [];

        // Get ONLY staff messages for students (no admin messages)
        $staff_stmt = $conn->prepare("SELECT id, sender_id, title, content, attachment, created_at, 'staff' as sender_type 
                                    FROM staffmessages 
                                    WHERE recipient_type = 'student' 
                                    AND (department IS NULL OR department = '' OR department = ?) 
                                    AND (year IS NULL OR year = '' OR year = ?) 
                                    ORDER BY created_at DESC");
        $staff_stmt->bind_param("ss", $department, $year);
        $staff_stmt->execute();
        $staff_result = $staff_stmt->get_result();
        
        while ($row = $staff_result->fetch_assoc()) {
            // Get sender name
            $sender_stmt = $conn->prepare("SELECT name FROM users WHERE user_id = ?");
            $sender_stmt->bind_param("s", $row['sender_id']);
            $sender_stmt->execute();
            $sender_result = $sender_stmt->get_result();
            $sender_name = $sender_result->num_rows > 0 ? $sender_result->fetch_assoc()['name'] : 'Staff';
            $sender_stmt->close();
            
            // Get user-specific status for this message
            $status_stmt = $conn->prepare("SELECT status FROM user_message_status WHERE user_id = ? AND message_id = ? AND message_table = 'staffmessages'");
            $status_stmt->bind_param("si", $user_id, $row['id']);
            $status_stmt->execute();
            $status_result = $status_stmt->get_result();
            $user_status = $status_result->num_rows > 0 ? $status_result->fetch_assoc()['status'] : 'unread';
            $status_stmt->close();

            // Skip deleted messages
            if ($user_status === 'deleted') continue;

            $messages[] = [
                'id' => $row['id'],
                'title' => $row['title'],
                'content' => $row['content'],
                'attachment' => $row['attachment'],
                'created_at' => $row['created_at'],
                'sender_type' => 'staff',
                'sender_id' => $row['sender_id'],
                'sender_name' => $sender_name,
                'message_table' => 'staffmessages',
                'user_status' => $user_status
            ];
        }
        $staff_stmt->close();

        $response['status'] = true;
        $response['message'] = 'Staff messages for students retrieved successfully.';
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