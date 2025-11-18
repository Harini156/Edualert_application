-- Fix password_reset table structure
-- The user_id should be VARCHAR(10) to match users table, not INT(11)

-- Step 1: Backup existing data (if any) - OPTIONAL
-- Uncomment this line if you want to see existing data before dropping table
-- SELECT * FROM password_reset;

-- Step 2: Drop the existing table with wrong structure
DROP TABLE IF EXISTS password_reset;

-- Step 3: Create new table with correct structure
CREATE TABLE password_reset (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(10) NOT NULL,
    otp VARCHAR(6) NOT NULL,
    expiry DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Step 4: Verify the new table structure
DESCRIBE password_reset;

-- Step 5: Confirm table is empty and ready
SELECT COUNT(*) as record_count FROM password_reset;