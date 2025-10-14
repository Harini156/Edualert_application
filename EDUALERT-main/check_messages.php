<?php
// Check message IDs in database
// Save this as check_messages.php in your server root

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

include('db.php');

echo "<h2>Messages Table:</h2>";
$sql = "SELECT id, title, status FROM messages LIMIT 5";
$result = $conn->query($sql);
if ($result->num_rows > 0) {
    echo "<table border='1'><tr><th>ID</th><th>Title</th><th>Status</th></tr>";
    while($row = $result->fetch_assoc()) {
        echo "<tr><td>" . $row["id"] . "</td><td>" . $row["title"] . "</td><td>" . $row["status"] . "</td></tr>";
    }
    echo "</table>";
} else {
    echo "No messages found";
}

echo "<br><h2>Staff Messages Table:</h2>";
$sql2 = "SELECT id, title, status FROM staffmessages LIMIT 5";
$result2 = $conn->query($sql2);
if ($result2->num_rows > 0) {
    echo "<table border='1'><tr><th>ID</th><th>Title</th><th>Status</th></tr>";
    while($row = $result2->fetch_assoc()) {
        echo "<tr><td>" . $row["id"] . "</td><td>" . $row["title"] . "</td><td>" . $row["status"] . "</td></tr>";
    }
    echo "</table>";
} else {
    echo "No staff messages found";
}

$conn->close();
?>
