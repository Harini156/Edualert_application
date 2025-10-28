<?php
// Simple PHP File Manager
// Password: admin123 (change this!)
session_start();

$password = "admin123"; // CHANGE THIS PASSWORD!
$logged_in = isset($_SESSION['logged_in']) && $_SESSION['logged_in'] === true;

// Handle login
if (isset($_POST['password'])) {
    if ($_POST['password'] === $password) {
        $_SESSION['logged_in'] = true;
        $logged_in = true;
    } else {
        $error = "Wrong password!";
    }
}

// Handle logout
if (isset($_GET['logout'])) {
    session_destroy();
    header('Location: ' . $_SERVER['PHP_SELF']);
    exit;
}

// Handle file upload
if ($logged_in && isset($_FILES['upload_file'])) {
    $target_dir = isset($_POST['upload_dir']) ? $_POST['upload_dir'] : './';
    $target_file = $target_dir . basename($_FILES['upload_file']['name']);
    
    if (move_uploaded_file($_FILES['upload_file']['tmp_name'], $target_file)) {
        $success = "File uploaded successfully: " . basename($_FILES['upload_file']['name']);
    } else {
        $error = "File upload failed!";
    }
}

// Handle file creation
if ($logged_in && isset($_POST['create_file'])) {
    $file_path = $_POST['file_path'];
    $file_content = $_POST['file_content'];
    
    if (file_put_contents($file_path, $file_content)) {
        $success = "File created successfully: " . $file_path;
    } else {
        $error = "Failed to create file!";
    }
}

// Get current directory
$current_dir = isset($_GET['dir']) ? $_GET['dir'] : './';
$current_dir = realpath($current_dir) . '/';

?>
<!DOCTYPE html>
<html>
<head>
    <title>Simple File Manager</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 1200px; margin: 0 auto; }
        .login-form { max-width: 300px; margin: 100px auto; padding: 20px; border: 1px solid #ccc; }
        .file-list { margin: 20px 0; }
        .file-item { padding: 5px; border-bottom: 1px solid #eee; }
        .folder { color: #0066cc; font-weight: bold; }
        .file { color: #333; }
        .upload-form { background: #f5f5f5; padding: 15px; margin: 20px 0; }
        .create-form { background: #e8f4fd; padding: 15px; margin: 20px 0; }
        .success { color: green; background: #d4edda; padding: 10px; margin: 10px 0; }
        .error { color: red; background: #f8d7da; padding: 10px; margin: 10px 0; }
        textarea { width: 100%; height: 200px; }
        input[type="text"], input[type="file"] { width: 300px; padding: 5px; }
        button { padding: 8px 15px; margin: 5px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🗂️ Simple File Manager</h1>
        
        <?php if (!$logged_in): ?>
            <!-- Login Form -->
            <div class="login-form">
                <h2>Login Required</h2>
                <?php if (isset($error)): ?>
                    <div class="error"><?php echo $error; ?></div>
                <?php endif; ?>
                <form method="post">
                    <p>
                        <label>Password:</label><br>
                        <input type="password" name="password" required>
                    </p>
                    <button type="submit">Login</button>
                </form>
            </div>
        <?php else: ?>
            <!-- File Manager Interface -->
            <div style="text-align: right;">
                <a href="?logout=1">Logout</a>
            </div>
            
            <?php if (isset($success)): ?>
                <div class="success"><?php echo $success; ?></div>
            <?php endif; ?>
            
            <?php if (isset($error)): ?>
                <div class="error"><?php echo $error; ?></div>
            <?php endif; ?>
            
            <h2>📁 Current Directory: <?php echo $current_dir; ?></h2>
            
            <!-- File Upload Form -->
            <div class="upload-form">
                <h3>📤 Upload File</h3>
                <form method="post" enctype="multipart/form-data">
                    <input type="hidden" name="upload_dir" value="<?php echo $current_dir; ?>">
                    <input type="file" name="upload_file" required>
                    <button type="submit">Upload</button>
                </form>
            </div>
            
            <!-- Create File Form -->
            <div class="create-form">
                <h3>📝 Create New File</h3>
                <form method="post">
                    <p>
                        <label>File Path:</label><br>
                        <input type="text" name="file_path" placeholder="<?php echo $current_dir; ?>filename.php" required>
                    </p>
                    <p>
                        <label>File Content:</label><br>
                        <textarea name="file_content" placeholder="Enter file content here..."></textarea>
                    </p>
                    <button type="submit" name="create_file">Create File</button>
                </form>
            </div>
            
            <!-- File List -->
            <div class="file-list">
                <h3>📋 Files and Folders</h3>
                
                <?php if ($current_dir !== realpath('./') . '/'): ?>
                    <div class="file-item">
                        <a href="?dir=<?php echo dirname($current_dir); ?>" class="folder">📁 .. (Parent Directory)</a>
                    </div>
                <?php endif; ?>
                
                <?php
                $files = scandir($current_dir);
                foreach ($files as $file) {
                    if ($file === '.' || $file === '..') continue;
                    
                    $file_path = $current_dir . $file;
                    $is_dir = is_dir($file_path);
                    $file_size = $is_dir ? '' : ' (' . number_format(filesize($file_path)) . ' bytes)';
                    
                    echo '<div class="file-item">';
                    if ($is_dir) {
                        echo '<a href="?dir=' . $file_path . '" class="folder">📁 ' . $file . '</a>';
                    } else {
                        echo '<span class="file">📄 ' . $file . $file_size . '</span>';
                        if (pathinfo($file, PATHINFO_EXTENSION) === 'php') {
                            echo ' <a href="' . str_replace($_SERVER['DOCUMENT_ROOT'], '', $file_path) . '" target="_blank">[View]</a>';
                        }
                    }
                    echo '</div>';
                }
                ?>
            </div>
            
            <!-- Server Info -->
            <div style="margin-top: 40px; padding: 15px; background: #f8f9fa; border: 1px solid #dee2e6;">
                <h3>🖥️ Server Information</h3>
                <p><strong>Server Root:</strong> <?php echo $_SERVER['DOCUMENT_ROOT']; ?></p>
                <p><strong>Current Script:</strong> <?php echo __FILE__; ?></p>
                <p><strong>PHP Version:</strong> <?php echo PHP_VERSION; ?></p>
                <p><strong>Server Software:</strong> <?php echo $_SERVER['SERVER_SOFTWARE']; ?></p>
            </div>
            
        <?php endif; ?>
    </div>
</body>
</html>