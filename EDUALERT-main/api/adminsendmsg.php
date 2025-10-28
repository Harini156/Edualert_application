<?php
header('Content-Type: application/json');
include 'db.php';

$response = [];

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $title = isset($_POST['title']) ? trim($_POST['title']) : '';
    $content = isset($_POST['content']) ? trim($_POST['content']) : '';
    $recipient_type = isset($_POST['recipient_type']) ? trim($_POST['recipient_type']) : '';

    if (empty($title) || empty($content) || empty($recipient_type)) {
        $response['success'] = false;
        $response['message'] = 'Title, content, and recipient type are required.';
        echo json_encode($response);
        exit;
    }

    $department = isset($_POST['department']) ? trim($_POST['department']) : null;
    $staff_type = isset($_POST['staff_type']) ? trim($_POST['staff_type']) : null;
    $designation = isset($_POST['designation']) ? trim($_POST['designation']) : null;
    $year = isset($_POST['year']) ? trim($_POST['year']) : null;
    $stay_type = isset($_POST['stay_type']) ? trim($_POST['stay_type']) : null;
    $gender = isset($_POST['gender']) ? trim($_POST['gender']) : null;
    $cgpa = isset($_POST['cgpa']) ? trim($_POST['cgpa']) : null;
    $backlogs = isset($_POST['backlogs']) ? trim($_POST['backlogs']) : null;

    if ($department && stripos($department, 'select') !== false) $department = null;
    if ($staff_type && stripos($staff_type, 'select') !== false) $staff_type = null;
    if ($designation && stripos($designation, 'select') !== false) $designation = null;
    if ($year && stripos($year, 'select') !== false) $year = null;
    if ($stay_type && stripos($stay_type, 'select') !== false) $stay_type = null;
    if ($gender && stripos($gender, 'select') !== false) $gender = null;
    if ($cgpa && stripos($cgpa, 'select') !== false) $cgpa = null;
    if ($backlogs && stripos($backlogs, 'select') !== false) $backlogs = null;

    $attachment_path = null;
    if (!empty($_FILES['attachment']['name'])) {
        $upload_dir = 'uploads/';
        
        if (!file_exists($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }
        
        $file_name = basename($_FILES['attachment']['name']);
        $file_ext = strtolower(pathinfo($file_name, PATHINFO_EXTENSION));
        $unique_filename = time() . '_' . uniqid() . '_' . $file_name;
        $target_path = $upload_dir . $unique_filename;
        
        $allowed_extensions = ['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png', 'txt', 'xls', 'xlsx'];
        if (!in_array($file_ext, $allowed_extensions)) {
            $response['success'] = false;
            $response['message'] = 'File type not allowed.';
            echo json_encode($response);
            exit;
        }
        
        if ($_FILES['attachment']['size'] > 10 * 1024 * 1024) {
            $response['success'] = false;
            $response['message'] = 'File size too large.';
            echo json_encode($response);
            exit;
        }
        
        if (move_uploaded_file($_FILES['attachment']['tmp_name'], $target_path)) {
            $attachment_path = 'api/uploads/' . $unique_filename;
        } else {
            $response['success'] = false;
            $response['message'] = 'Failed to upload attachment.';
            echo json_encode($response);
            exit;
        }
    }

    $sql = "INSERT INTO messages (title, content, recipient_type, department, staff_type, designation, year, stay_type, gender, cgpa, backlogs, attachment, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'unread')";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        $response['success'] = false;
        $response['message'] = 'Database error occurred.';
        echo json_encode($response);
        exit;
    }
    
    $stmt->bind_param("ssssssssssss", $title, $content, $recipient_type, $department, $staff_type, $designation, $year, $stay_type, $gender, $cgpa, $backlogs, $attachment_path);
    
    if ($stmt->execute()) {
        $response['success'] = true;
        $response['message'] = 'Message sent successfully!';
    } else {
        $response['success'] = false;
        $response['message'] = 'Failed to send message.';
    }
    
    $stmt->close();
    $conn->close();
} else {
    $response['success'] = false;
    $response['message'] = 'Invalid request method.';
}

echo json_encode($response);
?>
