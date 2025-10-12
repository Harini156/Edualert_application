-- Execute these SQL commands in your database:

-- 1. Add status column to messages table
ALTER TABLE messages ADD COLUMN status ENUM('read', 'unread') DEFAULT 'unread';

-- 2. Add status column to staffmessages table  
ALTER TABLE staffmessages ADD COLUMN status ENUM('read', 'unread') DEFAULT 'unread';

-- 3. Update all existing messages to 'unread' status
UPDATE messages SET status = 'unread' WHERE status IS NULL;
UPDATE staffmessages SET status = 'unread' WHERE status IS NULL;

-- 4. Verify the changes
DESCRIBE messages;
DESCRIBE staffmessages;
