-- ============================================
-- CampusHub 开发环境 H2 建表脚本
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
    gender VARCHAR(20),
    campus VARCHAR(100),
    credit_score INT NOT NULL DEFAULT 100,
    publisher_rating DECIMAL(3,2) NOT NULL DEFAULT 0,
    publisher_rating_count INT NOT NULL DEFAULT 0,
    acceptor_rating DECIMAL(3,2) NOT NULL DEFAULT 0,
    acceptor_rating_count INT NOT NULL DEFAULT 0,
    quota_available INT NOT NULL DEFAULT 0,
    quota_frozen INT NOT NULL DEFAULT 0,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_CERT',
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS certifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cert_type VARCHAR(20),
    student_id VARCHAR(20),
    gender VARCHAR(20),
    card_image_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reject_reason VARCHAR(255),
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS credit_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30),
    change_amount INT NOT NULL,
    before_score INT,
    after_score INT,
    reason VARCHAR(255),
    related_order_id BIGINT,
    operator_id BIGINT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quota_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    change_amount INT NOT NULL,
    available_after INT NOT NULL,
    frozen_after INT NOT NULL,
    related_demand_id BIGINT,
    related_order_id BIGINT,
    operator_id BIGINT,
    reason VARCHAR(255),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_key VARCHAR(80) UNIQUE,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    related_demand_id BIGINT,
    last_message_content VARCHAR(500),
    last_message_at TIMESTAMP,
    created_at TIMESTAMP,
    unread_count BIGINT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    blocked_by_user_id BIGINT,
    user1_last_read_message_id BIGINT DEFAULT 0,
    user2_last_read_message_id BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    client_message_id VARCHAR(80),
    content TEXT,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    quoted_message_id BIGINT,
    quoted_content VARCHAR(500),
    "read" TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    UNIQUE(sender_id, client_message_id)
);

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
    message_type VARCHAR(20),
    message_image_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    resolution TEXT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS credit_appeals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS demands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    publisher_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(30),
    reward_amount INT,
    reward_type VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    urgent TINYINT NOT NULL DEFAULT 0,
    urgent_deadline TIMESTAMP,
    location_tag VARCHAR(100),
    campus VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS demand_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demand_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demand_id BIGINT NOT NULL,
    active_demand_id BIGINT UNIQUE,
    publisher_id BIGINT NOT NULL,
    acceptor_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACCEPT',
    accepted_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason VARCHAR(500),
    auto_confirm_deadline TIMESTAMP,
    acceptor_deposit_amount INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_status_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    operator_id BIGINT,
    remark VARCHAR(500),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS arbitration_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT,
    order_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason TEXT,
    evidence_urls TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    resolution TEXT,
    resolved_by BIGINT,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    reviewer_role VARCHAR(20) NOT NULL,
    rating INT NOT NULL,
    content TEXT,
    image_urls TEXT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    related_id BIGINT,
    "read" TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    operation VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    detail TEXT,
    created_at TIMESTAMP
);
