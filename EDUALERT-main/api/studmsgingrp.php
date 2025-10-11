<?php
header('Content-Type: application/json');
include 'db.php';
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
    $target_path = $upload_dir . time() . "_" . $file_name;

    if (move_uploaded_file($file_tmp, $target_path)) {
        $attachment_path = $target_path;
    } else {
        echo json_encode(["status" => "error", "message" => "Attachment upload failed"]);
        exit;
    }
}

// ✅ Insert the message
$stmt = $conn->prepare("INSERT INTO studgrpmsg (student_id, group_id, title, message, attachment) VALUES (?, ?, ?, ?, ?)");
$stmt->bind_param("sisss", $student_id, $group_id, $title, $message, $attachment_path);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Message sent to group"]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to send message"]);
}

$stmt->close();
$conn->close();
?>
