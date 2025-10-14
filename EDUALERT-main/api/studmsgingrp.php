<?php
header('Content-Type: application/json');
include 'db.php';
include 'notification_helper.php'; // Notification helper functions
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

$student_id = $_POST['student_id'] ?? '';
$group_id = $_POST['group_id'] ?? '';
$title = $_POST['title'] ?? '';
$message = $_POST['message'] ?? '';
$attachment_path = null;

// Validate input
if (!$student_id || !$group_id || !$message) {
    echo json_encode(["status" => "error", "message" => "Required fields missing"]);
    exit;
}

// ✅ Check if student belongs to the group
$check = $conn->prepare("SELECT * FROM group_members WHERE user_id = ? AND group_id = ?");
$check->bind_param("si", $student_id, $group_id);
$check->execute();
$res = $check->get_result();

if ($res->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "You are not part of this group"]);
    exit;
}

// ✅ Handle file upload if present
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
            'image/jpg' => 'jpg',
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
        $attachment_path = $target_path; // store relative path
    } else {
        echo json_encode(["status" => "error", "message" => "Attachment upload failed"]);
        exit;
    }
}

// ✅ Insert the message
$stmt = $conn->prepare("INSERT INTO studgrpmsg (student_id, group_id, title, message, attachment) VALUES (?, ?, ?, ?, ?)");
$stmt->bind_param("sisss", $student_id, $group_id, $title, $message, $attachment_path);

if ($stmt->execute()) {
    // Create notifications for all other group members
    $notifications_created = 0;
    
    // Get all group members except the sender
    $members_stmt = $conn->prepare("SELECT user_id FROM group_members WHERE group_id = ? AND user_id != ?");
    $members_stmt->bind_param("is", $group_id, $student_id);
    $members_stmt->execute();
    $members_result = $members_stmt->get_result();
    
    while ($member_row = $members_result->fetch_assoc()) {
        if (createNotificationForMessage($conn, "Group Message: " . ($title ?: "No Title"), $message, 'student', $member_row['user_id'])) {
            $notifications_created++;
        }
    }
    $members_stmt->close();
    
    echo json_encode([
        "status" => "success", 
        "message" => "Message sent to group",
        "notifications_created" => $notifications_created
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to send message"]);
}

$stmt->close();
$conn->close();
?>
