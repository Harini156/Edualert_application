<?php
header('Content-Type: application/json');
include 'db.php';
include 'notification_helper.php'; // Notification helper functions
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

$sender_id    = $_POST['staff_id'] ?? '';
$receiver_type = $_POST['receiver_type'] ?? ''; // student, group, department, year, dept_year
$receiver_raw = $_POST['receiver_id'] ?? '';
$title        = $_POST['title'] ?? '';
$message      = $_POST['message'] ?? '';
$attachment_path = null;

if (!$sender_id || !$receiver_type || !$receiver_raw || !$title || !$message) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$is_group_message = 0;

// 📦 Handle file upload if present
if (!empty($_FILES['attachment']['name'])) {
    $upload_dir = "uploads/";
    if (!file_exists($upload_dir)) {
        mkdir($upload_dir, 0777, true);
    }

    $file_tmp = $_FILES['attachment']['tmp_name'];
    $file_name = basename($_FILES['attachment']['name']);
    $ext = pathinfo($file_name, PATHINFO_EXTENSION);
    if ($ext === '' || $ext === null) {
        $mime = mime_content_type($file_tmp);
        $map = [
            'image/jpeg' => 'jpg',
            'image/png' => 'png',
            'application/pdf' => 'pdf',
            'application/msword' => 'doc',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document' => 'docx',
            'application/vnd.ms-excel' => 'xls',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' => 'xlsx'
        ];
        $guessed = $map[$mime] ?? '';
        if ($guessed !== '') {
            $file_name .= '.' . $guessed;
        }
    }
    $target_path = $upload_dir . time() . "_" . $file_name;

    if (move_uploaded_file($file_tmp, $target_path)) {
        $attachment_path = $target_path;
    } else {
        echo json_encode(["status" => "error", "message" => "Attachment upload failed"]);
        exit;
    }
}

$inserted = 0;
$notifications_created = 0;

if ($receiver_type === "student") {
    // Send to single student
    $stmt = $conn->prepare("INSERT INTO staffmessages (sender_id, receiver_id, title, message, attachment, is_group_message) VALUES (?, ?, ?, ?, ?, 0)");
    $stmt->bind_param("sssss", $sender_id, $receiver_raw, $title, $message, $attachment_path);
    if ($stmt->execute()) {
        $inserted++;
        // Create notification for the student
        if (createNotificationForMessage($conn, "Message from Staff: " . $title, $message, 'student', $receiver_raw)) {
            $notifications_created++;
        }
    }

} elseif ($receiver_type === "group") {
    // Send to group (check if staff owns it)
    $check = $conn->prepare("SELECT group_id FROM grpstaff WHERE group_id = ? AND created_by = ?");
    $check->bind_param("is", $receiver_raw, $sender_id);
    $check->execute();
    $res = $check->get_result();

    if ($res->num_rows > 0) {
        $stmt = $conn->prepare("INSERT INTO staffmessages (sender_id, receiver_id, title, message, attachment, is_group_message) VALUES (?, ?, ?, ?, ?, 1)");
        $stmt->bind_param("sssss", $sender_id, $receiver_raw, $title, $message, $attachment_path);
        if ($stmt->execute()) {
            $inserted++;
            // Create notifications for all group members
            $notifications_created += createStaffMessageNotifications($conn, $sender_id, 'group', $receiver_raw, $title, $message);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "You don't have access to this group"]);
        exit;
    }

} elseif ($receiver_type === "department") {
    // Send to all students in that department
    $stmt = $conn->prepare("SELECT user_id FROM students WHERE department = ?");
    $stmt->bind_param("s", $receiver_raw);
    $stmt->execute();
    $result = $stmt->get_result();

    while ($row = $result->fetch_assoc()) {
        $receiver_id = $row['user_id'];
        $s = $conn->prepare("INSERT INTO staffmessages (sender_id, receiver_id, title, message, attachment, is_group_message) VALUES (?, ?, ?, ?, ?, 0)");
        $s->bind_param("sssss", $sender_id, $receiver_id, $title, $message, $attachment_path);
        if ($s->execute()) $inserted++;
    }
    
    // Create notifications for all students in department
    $notifications_created += createStaffMessageNotifications($conn, $sender_id, 'department', $receiver_raw, $title, $message);

} elseif ($receiver_type === "year") {
    // Send to all students in that year
    $stmt = $conn->prepare("SELECT user_id FROM students WHERE year = ?");
    $stmt->bind_param("i", $receiver_raw);
    $stmt->execute();
    $result = $stmt->get_result();

    while ($row = $result->fetch_assoc()) {
        $receiver_id = $row['user_id'];
        $s = $conn->prepare("INSERT INTO staffmessages (sender_id, receiver_id, title, message, attachment, is_group_message) VALUES (?, ?, ?, ?, ?, 0)");
        $s->bind_param("sssss", $sender_id, $receiver_id, $title, $message, $attachment_path);
        if ($s->execute()) $inserted++;
    }
    
    // Create notifications for all students in year
    $notifications_created += createStaffMessageNotifications($conn, $sender_id, 'year', $receiver_raw, $title, $message);

} elseif ($receiver_type === "dept_year") {
    // receiver_raw like CSE-2
    [$dept, $yr] = explode("-", $receiver_raw);
    $stmt = $conn->prepare("SELECT user_id FROM students WHERE department = ? AND year = ?");
    $stmt->bind_param("si", $dept, $yr);
    $stmt->execute();
    $result = $stmt->get_result();

    while ($row = $result->fetch_assoc()) {
        $receiver_id = $row['user_id'];
        $s = $conn->prepare("INSERT INTO staffmessages (sender_id, receiver_id, title, message, attachment, is_group_message) VALUES (?, ?, ?, ?, ?, 0)");
        $s->bind_param("sssss", $sender_id, $receiver_id, $title, $message, $attachment_path);
        if ($s->execute()) $inserted++;
    }
    
    // Create notifications for all students in dept_year
    $notifications_created += createStaffMessageNotifications($conn, $sender_id, 'dept_year', $receiver_raw, $title, $message);
}

if ($inserted > 0) {
    echo json_encode([
        "status" => "success", 
        "message" => "Message sent successfully to $inserted recipient(s)",
        "notifications_created" => $notifications_created
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "No messages sent"]);
}
?>
