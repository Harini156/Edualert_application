<?php
// Updated: <?php echo date('Y-m-d H:i:s'); ?> - Testing deployment
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

include('db.php');

$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $user_type = isset($_POST['user_type']) ? trim($_POST['user_type']) : '';
    $user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
    $department = isset($_POST['department']) ? trim($_POST['department']) : null;
    $year = isset($_POST['year']) ? trim($_POST['year']) : null;
    $staff_type = isset($_POST['staff_type']) ? trim($_POST['staff_type']) : null;
    $designation = isset($_POST['designation']) ? trim($_POST['designation']) : null;
    
    if (empty($user_type) || empty($user_id)) {
        echo json_encode([
            "status" => "error",
            "message" => "User type and user ID are required."
        ]);
        exit;
    }
    
    // Validate user type
    if (!in_array($user_type, ['student', 'staff', 'admin'])) {
        echo json_encode([
            "status" => "error",
            "message" => "Invalid user type."
        ]);
        exit;
    }
    
    $total_unread_count = 0;
    $messages_count = 0;
    $staffmessages_count = 0;
    
    // Count from messages table (Admin broadcast messages)
    if ($user_type === 'student') {
        // For students: count admin messages targeted to students with matching filters
        $messages_sql = "
            SELECT COUNT(*) as count 
            FROM messages 
            WHERE status = 'unread' 
            AND (recipient_type = 'student' OR recipient_type = 'both')
            AND (department IS NULL OR department = '' OR department = ?)
            AND (year IS NULL OR year = '' OR year = ?)
        ";
        $stmt = $conn->prepare($messages_sql);
        if ($stmt) {
            $stmt->bind_param("ss", $department, $year);
            $stmt->execute();
            $result = $stmt->get_result();
            $row = $result->fetch_assoc();
            $messages_count = (int)$row['count'];
            $stmt->close();
        }
        
        // For students: count staff messages targeted to students
        $staffmessages_sql = "
            SELECT COUNT(*) as count 
            FROM staffmessages 
            WHERE status = 'unread' 
            AND recipient_type = 'student'
            AND (department IS NULL OR department = '' OR department = ?)
            AND (year IS NULL OR year = '' OR year = ?)
        ";
        $stmt = $conn->prepare($staffmessages_sql);
        if ($stmt) {
            $stmt->bind_param("ss", $department, $year);
            $stmt->execute();
            $result = $stmt->get_result();
            $row = $result->fetch_assoc();
            $staffmessages_count = (int)$row['count'];
            $stmt->close();
        }
        
    } elseif ($user_type === 'staff') {
        // For staff: count admin messages targeted to staff with matching filters
        $messages_sql = "
            SELECT COUNT(*) as count 
            FROM messages 
            WHERE status = 'unread' 
            AND (recipient_type = 'staff' OR recipient_type = 'both')
            AND (department IS NULL OR department = '' OR department = ?)
            AND (staff_type IS NULL OR staff_type = '' OR staff_type = ?)
            AND (designation IS NULL OR designation = '' OR designation = ?)
        ";
        $stmt = $conn->prepare($messages_sql);
        if ($stmt) {
            $stmt->bind_param("sss", $department, $staff_type, $designation);
            $stmt->execute();
            $result = $stmt->get_result();
            $row = $result->fetch_assoc();
            $messages_count = (int)$row['count'];
            $stmt->close();
        }
        
        // For staff: count staff messages targeted to staff
        $staffmessages_sql = "
            SELECT COUNT(*) as count 
            FROM staffmessages 
            WHERE status = 'unread' 
            AND recipient_type = 'staff'
            AND (department IS NULL OR department = '' OR department = ?)
            AND (designation IS NULL OR designation = '' OR designation = ?)
        ";
        $stmt = $conn->prepare($staffmessages_sql);
        if ($stmt) {
            $stmt->bind_param("ss", $department, $designation);
            $stmt->execute();
            $result = $stmt->get_result();
            $row = $result->fetch_assoc();
            $staffmessages_count = (int)$row['count'];
            $stmt->close();
        }
        
    } elseif ($user_type === 'admin') {
        // Admins don't receive messages, they only send them
        $messages_count = 0;
        $staffmessages_count = 0;
    }
    
    $total_unread_count = $messages_count + $staffmessages_count;
    
    echo json_encode([
        "status" => "success",
        "unread_count" => $total_unread_count,
        "messages_count" => $messages_count,
        "staffmessages_count" => $staffmessages_count,
        "debug" => [
            "user_type" => $user_type,
            "user_id" => $user_id,
            "department" => $department,
            "year" => $year,
            "staff_type" => $staff_type,
            "designation" => $designation
        ]
    ]);
    
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid request method. Expected POST."
    ]);
}

$conn->close();
?>
