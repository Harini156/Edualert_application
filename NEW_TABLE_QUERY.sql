-- ========================================
-- NEW TABLE FOR INDIVIDUAL MESSAGE STATUS
-- ========================================

CREATE TABLE `user_message_status` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(10) NOT NULL,
  `message_id` int(11) NOT NULL,
  `message_table` enum('messages','staffmessages') NOT NULL,
  `status` enum('unread','read','deleted') NOT NULL DEFAULT 'unread',
  `marked_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_message` (`user_id`, `message_id`, `message_table`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_message_table` (`message_id`, `message_table`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ========================================
-- EXPLANATION:
-- ========================================
-- user_id: Links to users.user_id (STU001, STF001, etc.)
-- message_id: Links to messages.id or staffmessages.id
-- message_table: Identifies source ('messages' for admin, 'staffmessages' for staff)
-- status: 'unread' (default), 'read' (after tick), 'deleted' (after delete)
-- marked_at: Timestamp when status was last changed
-- unique_user_message: Prevents duplicate entries for same user+message
-- Indexes: Optimized for fast counting and filtering