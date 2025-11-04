<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

include('db.php'); // DB connection

try {
    // Get all admin sent messages (no admin_id filter needed for admin dashboard)
    $sql = "SELECT id, title, content, recipient_type, department, staff_type, designation, 
                   year, stay_type, gender, cgpa, backlogs, attachment, created_at 
            FROM messages 
            ORDER BY created_at DESC";
    
    $stmt = $conn->prepare($sql);
    $stmt->execute();
    $result = $stmt->get_result();

    $messages = [];

    while ($row = $result->fetch_assoc()) {
        $messages[] = [
            'id' => $row['id'],
            'title' => $row['title'],
            'content' => $row['content'],
            'recipient_type' => $row['recipient_type'],
            'department' => $row['department'],
            'staff_type' => $row['staff_type'],
            'designation' => $row['designation'],
            'year' => $row['year'],
            'stay_type' => $row['stay_type'],
            'gender' => $row['gender'],
            'cgpa' => $row['cgpa'],
            'backlogs' => $row['backlogs'],
            'attachment' => $row['attachment'],
            'created_at' => $row['created_at']
        ];
    }

    echo json_encode([
        "status" => "success",
        "messages" => $messages
    ]);

    $stmt->close();
    
} catch (Exception $e) {
    echo json_encode([
        "status" => "error", 
        "message" => "Database error occurred."
    ]);
}

$conn->close();
?>
