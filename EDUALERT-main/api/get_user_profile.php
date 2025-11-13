<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

include('db.php');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
    
    if (empty($user_id)) {
        echo json_encode([
            "status" => "error",
            "message" => "User ID is required."
        ]);
        exit;
    }
    
    // Get basic user info
    $user_sql = "SELECT user_id, name, email, user_type, dept, year FROM users WHERE user_id = ?";
    $stmt = $conn->prepare($user_sql);
    
    if (!$stmt) {
        echo json_encode([
            "status" => "error",
            "message" => "Database error: " . $conn->error
        ]);
        exit;
    }
    
    $stmt->bind_param("s", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        echo json_encode([
            "status" => "error",
            "message" => "User not found."
        ]);
        exit;
    }
    
    $user = $result->fetch_assoc();
    $stmt->close();
    
    $response = [
        "status" => "success",
        "user_id" => $user['user_id'],
        "name" => $user['name'],
        "email" => $user['email'],
        "user_type" => $user['user_type'],
        "department" => $user['dept'],
        "year" => $user['year']
    ];
    
    // Get additional details based on user type
    if ($user['user_type'] === 'student') {
        $details_sql = "SELECT department, year, cgpa, backlogs, stay_type, gender FROM student_details WHERE user_id = ?";
        $stmt = $conn->prepare($details_sql);
        if ($stmt) {
            $stmt->bind_param("s", $user_id);
            $stmt->execute();
            $details_result = $stmt->get_result();
            if ($details_result->num_rows > 0) {
                $details = $details_result->fetch_assoc();
                $response['department'] = $details['department'];
                $response['year'] = $details['year'];
                $response['cgpa'] = $details['cgpa'];
                $response['backlogs'] = $details['backlogs'];
                $response['stay_type'] = $details['stay_type'];
                $response['gender'] = $details['gender'];
            }
            $stmt->close();
        }
    } elseif ($user['user_type'] === 'staff') {
        $details_sql = "SELECT department, staff_type, designation FROM staff_details WHERE user_id = ?";
        $stmt = $conn->prepare($details_sql);
        if ($stmt) {
            $stmt->bind_param("s", $user_id);
            $stmt->execute();
            $details_result = $stmt->get_result();
            if ($details_result->num_rows > 0) {
                $details = $details_result->fetch_assoc();
                $response['department'] = $details['department'];
                $response['staff_type'] = $details['staff_type'];
                $response['designation'] = $details['designation'];
            }
            $stmt->close();
        }
    }
    
    echo json_encode($response);
    
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid request method. Expected POST."
    ]);
}

$conn->close();
?>