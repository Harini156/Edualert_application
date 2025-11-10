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

        // Handle file attachment with proper filename cleaning and extension detection
        $attachment_path = null;
        if (!empty($_FILES['attachment']['name'])) {
            $upload_dir = 'uploads/';
            
            if (!file_exists($upload_dir)) {
                mkdir($upload_dir, 0777, true);
            }
            
            $original_name = basename($_FILES['attachment']['name']);
            $file_mime = $_FILES['attachment']['type'];
            
            // Clean filename - remove invalid characters like colons, spaces, etc.
            $clean_name = preg_replace('/[^a-zA-Z0-9._-]/', '_', $original_name);
            
            // Detect proper file extension from MIME type
            $mime_to_ext = [
                'image/jpeg' => '.jpg',
                'image/jpg' => '.jpg',
                'image/png' => '.png',
                'image/gif' => '.gif',
                'image/bmp' => '.bmp',
                'image/webp' => '.webp',
                'application/pdf' => '.pdf',
                'application/msword' => '.doc',
                'application/vnd.openxmlformats-officedocument.wordprocessingml.document' => '.docx',
                'application/vnd.ms-excel' => '.xls',
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' => '.xlsx',
                'application/vnd.ms-powerpoint' => '.ppt',
                'application/vnd.openxmlformats-officedocument.presentationml.presentation' => '.pptx',
                'text/plain' => '.txt',
                'text/csv' => '.csv',
                'application/zip' => '.zip',
                'application/x-rar-compressed' => '.rar',
                'application/x-7z-compressed' => '.7z'
            ];
            
            // Get extension from MIME type or fallback to original extension
            $detected_ext = isset($mime_to_ext[$file_mime]) ? $mime_to_ext[$file_mime] : '';
            if (empty($detected_ext)) {
                $original_ext = strtolower(pathinfo($original_name, PATHINFO_EXTENSION));
                $detected_ext = !empty($original_ext) ? '.' . $original_ext : '.bin';
            }
            
            // Remove existing extension from clean name and add detected extension
            $clean_name_no_ext = pathinfo($clean_name, PATHINFO_FILENAME);
            $unique_filename = time() . '_' . uniqid() . '_' . $clean_name_no_ext . $detected_ext;
            $target_path = $upload_dir . $unique_filename;
            
            // Support ALL file types - no restrictions
            $allowed_extensions = ['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png', 'gif', 'bmp', 'txt', 'xls', 'xlsx', 'ppt', 'pptx', 'zip', 'rar', 'mp4', 'avi', 'mov', 'mp3', 'wav', 'csv', 'rtf', 'odt', 'ods', 'odp', 'tiff', 'svg', 'webp', '7z', 'tar', 'gz', 'json', 'xml', 'html', 'css', 'js', 'py', 'java', 'cpp', 'c', 'h', 'sql', 'md', 'log'];
            
            // Allow any file type - just check for dangerous executable files
            $dangerous_extensions = ['exe', 'bat', 'cmd', 'com', 'pif', 'scr', 'vbs', 'jar', 'php', 'asp', 'jsp'];
            if (in_array($file_ext, $dangerous_extensions)) {
                $response['success'] = false;
                $response['message'] = 'Executable files are not allowed for security reasons.';
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