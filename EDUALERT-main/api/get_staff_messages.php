<?php
// Get Staff Messages - For staff to receive messages from other staff/HOD
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

        // Get staff details to determine department and designation
        $staff_stmt = $conn->prepare("SELECT u.dept, stf.department, stf.designation 
                                     FROM users u 
                                     LEFT JOIN staff_details stf ON u.user_id = stf.user_id 
                                     WHERE u.user_id = ? AND u.user_type = 'staff'");
        $staff_stmt->bind_param("s", $user_id);
        $staff_stmt->execute();
        $staff_result = $staff_stmt->get_result();
        
        if ($staff_result->num_rows == 0) {
            $response['status'] = false;
            $response['message'] = 'Staff not found.';
            echo json_encode($response);
            exit;
        }
        
        $staff_data = $staff_result->fetch_assoc();
        $department = $staff_data['department'] ?? $staff_data['dept'];
        $designation = $staff_data['designation'];
        $staff_stmt->close();

        // Get messages from staffmessages table targeted to this staff
        $stmt = $conn->prepare("SELECT id, sender_id, title, content, attachment, created_at, 'staff' as sender_type 
                              FROM staffmessages 
                              WHERE recipient_type = 'staff' 
                              AND (department IS NULL OR department = '' OR department = ?) 
                              AND (designation IS NULL OR designation = '' OR designation = ?) 
                              ORDER BY created_at DESC");
        $stmt->bind_param("ss", $department, $designation);
        $stmt->execute();
        $result = $stmt->get_result();
        
        $messages = [];
        while ($row = $result->fetch_assoc()) {
            // Get sender name
            $sender_stmt = $conn->prepare("SELECT name FROM users WHERE user_id = ?");
            $sender_stmt->bind_param("s", $row['sender_id']);
            $sender_stmt->execute();
            $sender_result = $sender_stmt->get_result();
            $sender_name = $sender_result->num_rows > 0 ? $sender_result->fetch_assoc()['name'] : 'Unknown';
            $sender_stmt->close();
            
            $messages[] = [
                'id' => $row['id'],
                'title' => $row['title'],
                'content' => $row['content'],
                'attachment' => $row['attachment'],
                'created_at' => $row['created_at'],
                'sender_type' => 'staff',
                'sender_id' => $row['sender_id'],
                'sender_name' => $sender_name
            ];
        }
        $stmt->close();

        $response['status'] = true;
        $response['message'] = 'Staff messages retrieved successfully.';
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