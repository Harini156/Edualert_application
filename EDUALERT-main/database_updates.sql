-- Add status column to messages table
ALTER TABLE messages ADD COLUMN status ENUM('read', 'unread') DEFAULT 'unread';

-- Add status column to staffmessages table  
ALTER TABLE staffmessages ADD COLUMN status ENUM('read', 'unread') DEFAULT 'unread';

-- Update all existing messages to 'unread' status
UPDATE messages SET status = 'unread' WHERE status IS NULL;
UPDATE staffmessages SET status = 'unread' WHERE status IS NULL;
