<?php
// notification_helper.php
// Helper function to create notifications when messages are sent

function createNotificationForMessage($conn, $title, $message, $user_type, $user_id) {
    $sql = "
        INSERT INTO notifications (title, message, user_type, user_id, status, created_at) 
        VALUES (?, ?, ?, ?, 'unread', NOW())
    ";
    
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        return false;
    }
    
    $stmt->bind_param("ssss", $title, $message, $user_type, $user_id);
    $result = $stmt->execute();
    $stmt->close();
    
    return $result;
}

function createNotificationsForMultipleUsers($conn, $title, $message, $user_data_array) {
    $success_count = 0;
    
    foreach ($user_data_array as $user_data) {
        if (createNotificationForMessage($conn, $title, $message, $user_data['user_type'], $user_data['user_id'])) {
            $success_count++;
        }
    }
    
    return $success_count;
}

// Function to create notifications when admin sends messages
function createAdminMessageNotifications($conn, $sender_id, $recipient_type, $subject, $message_content) {
    $notifications_created = 0;
    
    if ($recipient_type === 'student') {
        // Get all students
        $sql = "SELECT user_id FROM students";
        $result = $conn->query($sql);
        
        while ($row = $result->fetch_assoc()) {
            if (createNotificationForMessage($conn, "Message from Admin: " . $subject, $message_content, 'student', $row['user_id'])) {
                $notifications_created++;
            }
        }
        
    } elseif ($recipient_type === 'staff') {
        // Get all staff
        $sql = "SELECT user_id FROM staffs";
        $result = $conn->query($sql);
        
        while ($row = $result->fetch_assoc()) {
            if (createNotificationForMessage($conn, "Message from Admin: " . $subject, $message_content, 'staff', $row['user_id'])) {
                $notifications_created++;
            }
        }
        
    } elseif ($recipient_type === 'both') {
        // Get all students and staff
        $sql = "SELECT user_id, 'student' as user_type FROM students UNION SELECT user_id, 'staff' as user_type FROM staffs";
        $result = $conn->query($sql);
        
        while ($row = $result->fetch_assoc()) {
            if (createNotificationForMessage($conn, "Message from Admin: " . $subject, $message_content, $row['user_type'], $row['user_id'])) {
                $notifications_created++;
            }
        }
    }
    
    return $notifications_created;
}

// Function to create notifications when staff sends messages to students
function createStaffMessageNotifications($conn, $sender_id, $recipient_type, $recipient_id, $title, $message_content) {
    $notifications_created = 0;
    
    if ($recipient_type === 'student') {
        // Single student
        if (createNotificationForMessage($conn, "Message from Staff: " . $title, $message_content, 'student', $recipient_id)) {
            $notifications_created++;
        }
        
    } elseif ($recipient_type === 'department') {
        // All students in department
        $sql = "SELECT user_id FROM students WHERE department = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("s", $recipient_id);
        $stmt->execute();
        $result = $stmt->get_result();
        
        while ($row = $result->fetch_assoc()) {
            if (createNotificationForMessage($conn, "Message from Staff: " . $title, $message_content, 'student', $row['user_id'])) {
                $notifications_created++;
            }
        }
        $stmt->close();
        
    } elseif ($recipient_type === 'year') {
        // All students in year
        $sql = "SELECT user_id FROM students WHERE year = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("s", $recipient_id);
        $stmt->execute();
        $result = $stmt->get_result();
        
        while ($row = $result->fetch_assoc()) {
            if (createNotificationForMessage($conn, "Message from Staff: " . $title, $message_content, 'student', $row['user_id'])) {
                $notifications_created++;
            }
        }
        $stmt->close();
        
    } elseif ($recipient_type === 'group') {
        // All students in group
        $sql = "SELECT user_id FROM group_members WHERE group_id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $recipient_id);
        $stmt->execute();
        $result = $stmt->get_result();
        
        while ($row = $result->fetch_assoc()) {
            if (createNotificationForMessage($conn, "Message from Staff: " . $title, $message_content, 'student', $row['user_id'])) {
                $notifications_created++;
            }
        }
        $stmt->close();
        
    } elseif ($recipient_type === 'dept_year') {
        // All students in department-year combination
        [$dept, $yr] = explode("-", $recipient_id);
        $sql = "SELECT user_id FROM students WHERE department = ? AND year = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("si", $dept, $yr);
        $stmt->execute();
        $result = $stmt->get_result();
        
        while ($row = $result->fetch_assoc()) {
            if (createNotificationForMessage($conn, "Message from Staff: " . $title, $message_content, 'student', $row['user_id'])) {
                $notifications_created++;
            }
        }
        $stmt->close();
    }
    
    return $notifications_created;
}
?>
