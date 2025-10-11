<?php
header('Content-Type: application/json');
session_start();
include 'db.php'; // Database connection

$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Ensure admin is logged in
    if (!isset($_SESSION['usertype']) || $_SESSION['usertype'] !== 'admin') {
        $response['status'] = 'error';
        $response['message'] = 'Access denied. Admins only.';
        echo json_encode($response);
        exit;
    }

    $sender_id      = $_SESSION['user_id'];
    $recipient_type = $_POST['recipient_type'];  // student / staff / both
    $subject        = trim($_POST['subject']);
    $message        = trim($_POST['message']);
    $attachmentPath = null;

    // Basic validation
    if (empty($recipient_type) || empty($subject) || empty($message)) {
        $response['status'] = 'error';
        $response['message'] = 'All fields are required.';
        echo json_encode($response);
        exit;
    }

    if (!in_array($recipient_type, ['student', 'staff', 'both'])) {
        $response['status'] = 'error';
        $response['message'] = 'Invalid recipient type. Use student, staff, or both.';
        echo json_encode($response);
        exit;
    }

    // Handle attachment
    if (isset($_FILES['attachment']) && $_FILES['attachment']['error'] === 0) {
        $uploadDir = 'uploads/';
        if (!file_exists($uploadDir)) {
            mkdir($uploadDir, 0755, true);
        }

        $filename = basename($_FILES['attachment']['name']);
        $targetFile = $uploadDir . time() . '_' . $filename;

        if (move_uploaded_file($_FILES['attachment']['tmp_name'], $targetFile)) {
            $attachmentPath = $targetFile;
        } else {
            $response['status'] = 'error';
            $response['message'] = 'File upload failed.';
            echo json_encode($response);
            exit;
        }
    }

    // Send to student, staff or both
    $recipientTypes = $recipient_type === 'both' ? ['student', 'staff'] : [$recipient_type];
    $successCount = 0;

    foreach ($recipientTypes as $type) {
        $stmt = $conn->prepare("INSERT INTO messages (sender_id, recipient_type, subject, message, attachment) VALUES (?, ?, ?, ?, ?)");
        $stmt->bind_param("sssss", $sender_id, $type, $subject, $message, $attachmentPath);

        if ($stmt->execute()) {
            $successCount++;
        }

        $stmt->close();
    }

    if ($successCount > 0) {
        $response['status'] = 'success';
        $response['message'] = 'Message sent successfully to ' . implode(' and ', $recipientTypes) . '.';
    } else {
        $response['status'] = 'error';
        $response['message'] = 'Message sending failed.';
    }

    $conn->close();
} else {
    $response['status'] = 'error';
    $response['message'] = 'Invalid request method.';
}

echo json_encode($response);
?>
