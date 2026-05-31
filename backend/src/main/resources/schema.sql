-- ============================================
-- CampusHub 数据库初始化脚本
-- 由 docker-compose 启动时自动执行
-- ============================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    student_id VARCHAR(20) UNIQUE,
    real_name VARCHAR(50),
    avatar_url VARCHAR(255),
    credit_score INT NOT NULL DEFAULT 100,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_CERT',
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_users_username (username),
    INDEX idx_users_student_id (student_id),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS certifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cert_type VARCHAR(20),
    student_id VARCHAR(20),
    card_image_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reject_reason VARCHAR(255),
    reviewed_by BIGINT,
    reviewed_at DATETIME,
    created_at DATETIME,
    INDEX idx_certifications_user_id (user_id),
    INDEX idx_certifications_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS credit_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    change_amount INT NOT NULL,
    reason VARCHAR(255),
    related_order_id BIGINT,
    created_at DATETIME,
    INDEX idx_credit_records_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_key VARCHAR(80) UNIQUE,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    related_demand_id BIGINT,
    last_message_content VARCHAR(500),
    last_message_at DATETIME,
    created_at DATETIME,
    unread_count BIGINT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    blocked_by_user_id BIGINT,
    user1_last_read_message_id BIGINT DEFAULT 0,
    user2_last_read_message_id BIGINT DEFAULT 0,
    INDEX idx_conversations_user1 (user1_id),
    INDEX idx_conversations_user2 (user2_id),
    INDEX idx_conversations_key (conversation_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    client_message_id VARCHAR(80),
    content MEDIUMTEXT,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    quoted_message_id BIGINT,
    quoted_content VARCHAR(500),
    `read` TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME,
    INDEX idx_messages_conversation (conversation_id),
    INDEX idx_messages_sender (sender_id),
    INDEX idx_messages_conversation_id (conversation_id, id),
    UNIQUE KEY uk_messages_sender_client (sender_id, client_message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS message_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    category VARCHAR(30) NOT NULL,
    reason TEXT,
    evidence_urls TEXT,
    message_snapshot TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at DATETIME,
    resolution TEXT,
    created_at DATETIME,
    INDEX idx_message_reports_status (status),
    INDEX idx_message_reports_reporter (reporter_id),
    INDEX idx_message_reports_message (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS demands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    publisher_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(30),
    reward_amount INT,
    reward_type VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    urgent TINYINT(1) NOT NULL DEFAULT 0,
    urgent_deadline DATETIME,
    location_tag VARCHAR(100),
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_demands_publisher (publisher_id),
    INDEX idx_demands_status_category (status, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS demand_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demand_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT,
    INDEX idx_demand_images_demand (demand_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demand_id BIGINT NOT NULL,
    publisher_id BIGINT NOT NULL,
    acceptor_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACCEPT',
    accepted_at DATETIME,
    completed_at DATETIME,
    cancelled_at DATETIME,
    cancel_reason VARCHAR(500),
    auto_confirm_deadline DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_orders_demand (demand_id),
    INDEX idx_orders_publisher (publisher_id),
    INDEX idx_orders_acceptor (acceptor_id),
    INDEX idx_orders_status_deadline (status, auto_confirm_deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_status_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    operator_id BIGINT,
    remark VARCHAR(500),
    created_at DATETIME,
    INDEX idx_order_status_logs_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS arbitration_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason TEXT,
    evidence_urls TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    resolution TEXT,
    resolved_by BIGINT,
    resolved_at DATETIME,
    created_at DATETIME,
    INDEX idx_arbitration_tickets_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    reviewer_role VARCHAR(20) NOT NULL,
    rating INT NOT NULL,
    content TEXT,
    image_urls TEXT,
    created_at DATETIME,
    INDEX idx_reviews_order (order_id),
    INDEX idx_reviews_reviewee (reviewee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    related_id BIGINT,
    `read` TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME,
    INDEX idx_notifications_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admin_operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    operation VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    detail TEXT,
    created_at DATETIME,
    INDEX idx_admin_logs_admin (admin_id),
    INDEX idx_admin_logs_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
