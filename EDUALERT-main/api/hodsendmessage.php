<?php
// Suppress all PHP warnings and errors to prevent JSON corruption
error_reporting(0);
ini_set('display_errors', 0);
ini_set('log_errors', 0);

// Start output buffering to catch any unexpected output
ob_start();

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Clear any previous output
ob_clean();

include 'db.php';

$response = [];

try {
    if ($_SERVER["REQUEST_METHOD"] == "POST") {
        $sender_id = isset($_POST['sender_id']) ? trim($_POST['sender_id']) : '';
        $title = isset($_POST['title']) ? trim($_POST['title']) : '';
        $content = isset($_POST['content']) ? trim($_POST['content']) : '';
        $recipient_type = isset($_POST['recipient_type']) ? trim($_POST['recipient_type']) : '';

        if (empty($sender_id) || empty($title) || empty($content) || empty($recipient_type)) {
            $response['success'] = false;
            $response['message'] = 'Sender ID, title, content, and recipient type are required.';
            echo json_encode($response);
            exit;
        }

        // Handle optional filters
        $department = isset($_POST['department']) ? trim($_POST['department']) : null;
        $year = isset($_POST['year']) ? trim($_POST['year']) : null;
        $designation = isset($_POST['designation']) ? trim($_POST['designation']) : null;

        // Clean "Select..." values
        if ($department && stripos($department, 'select') !== false) $department = null;
        if ($year && stripos($year, 'select') !== false) $year = null;
        if ($designation && stripos($designation, 'select') !== false) $designation = null;

        // Handle file attachment
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
            
            $allowed_extensions = ['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png', 'gif', 'bmp', 'txt', 'xls', 'xlsx', 'ppt', 'pptx', 'zip', 'rar'];
            if (!in_array($file_ext, $allowed_extensions)) {
                $response['success'] = false;
                $response['message'] = 'File type not allowed. Supported: PDF, DOC, DOCX, JPG, JPEG, PNG, GIF, BMP, TXT, XLS, XLSX, PPT, PPTX, ZIP, RAR';
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
                $attachment_path = 'uploads/' . $unique_filename;
            } else {
                $response['success'] = false;
                $response['message'] = 'Failed to upload attachment.';
                echo json_encode($response);
                exit;
            }
        }

        // Insert into staffmessages table
        $sql = "INSERT INTO staffmessages (sender_id, title, content, recipient_type, department, year, designation, attachment, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'unread')";
        
        $stmt = $conn->prepare($sql);
        
        if (!$stmt) {
            $response['success'] = false;
            $response['message'] = 'Database error occurred.';
            echo json_encode($response);
            exit;
        }
        
        $stmt->bind_param("ssssssss", $sender_id, $title, $content, $recipient_type, $department, $year, $designation, $attachment_path);
        
        if ($stmt->execute()) {
            $response['success'] = true;
            $response['message'] = 'Message sent successfully!';
        } else {
            $response['success'] = false;
            $response['message'] = 'Failed to send message.';
        }
        
        $stmt->close();
    } else {
        $response['success'] = false;
        $response['message'] = 'Invalid request method.';
    }
} catch (Exception $e) {
    $response['success'] = false;
    $response['message'] = 'Server error occurred.';
}

if (isset($conn)) {
    $conn->close();
}

// Clear any buffered output and send clean JSON
ob_clean();
echo json_encode($response);
ob_end_flush();
?>