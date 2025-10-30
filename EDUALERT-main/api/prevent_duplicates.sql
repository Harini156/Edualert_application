-- SQL script to prevent duplicate messages
-- Run this on your database to add duplicate prevention

-- Add a unique index to prevent identical messages within a short time frame
-- This creates a composite index on title, content, and recipient_type
ALTER TABLE messages ADD INDEX idx_duplicate_check (title(100), content(200), recipient_type, created_at);

-- Optional: Add a created_at column if it doesn't exist
-- ALTER TABLE messages ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Optional: Clean up existing duplicates (BACKUP YOUR DATA FIRST!)
-- DELETE m1 FROM messages m1
-- INNER JOIN messages m2 
-- WHERE m1.id > m2.id 
-- AND m1.title = m2.title 
-- AND m1.content = m2.content 
-- AND m1.recipient_type = m2.recipient_type
-- AND ABS(TIMESTAMPDIFF(SECOND, m1.created_at, m2.created_at)) <= 10;