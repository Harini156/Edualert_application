<?php
header('Content-Type: application/json');
session_start();
include 'db.php'; // Ensure this connects to your MySQL database

$response = [];

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $login_id = trim($_POST['login_id']); // can be email or user_id
    $password = $_POST['password'];

    if (empty($login_id) || empty($password)) {
        $response['status'] = 'error';
        $response['message'] = 'Email/User ID and password are required.';
        echo json_encode($response);
        exit;
    }

    $userTables = ['admins', 'staffs', 'students'];
    $userFound = false;

    foreach ($userTables as $table) {
        $stmt = $conn->prepare("SELECT id, name, email, password, usertype, user_id FROM $table WHERE email = ? OR user_id = ?");
        $stmt->bind_param("ss", $login_id, $login_id);
        $stmt->execute();
        $stmt->store_result();

        if ($stmt->num_rows > 0) {
            $stmt->bind_result($id, $name, $email, $hashedPassword, $usertype, $user_id);
            $stmt->fetch();

            if (password_verify($password, $hashedPassword)) {
                $_SESSION['user_id']  = $user_id;
                $_SESSION['name']     = $name;
                $_SESSION['email']    = $email;
                $_SESSION['usertype'] = $usertype;

                $response['status']  = 'success';
                $response['message'] = 'Login successful.';
                $response['user'] = [
                    'user_id'  => $user_id,
                    'email'    => $email,
                    'name'     => $name,
                    'usertype' => $usertype
                ];
                $userFound = true;
                break;
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Incorrect password.';
                echo json_encode($response);
                exit;
            }
        }
        $stmt->close();
    }

    if (!$userFound) {
        $response['status'] = 'error';
        $response['message'] = 'No user found with this Email or User ID.';
    }

    $conn->close();
} else {
    $response['status'] = 'error';
    $response['message'] = 'Invalid request method.';
}

echo json_encode($response);
?>
