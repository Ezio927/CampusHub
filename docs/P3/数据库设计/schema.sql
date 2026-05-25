
CREATE TYPE user_role AS ENUM ('STUDENT', 'ADMIN');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'BANNED', 'PENDING_CERT');
CREATE TYPE cert_type AS ENUM ('STUDENT_ID', 'STUDENT_CARD');
CREATE TYPE cert_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE demand_category AS ENUM ('ERRAND', 'SKILL', 'STUDY', 'SECONDHAND');
CREATE TYPE demand_status AS ENUM ('OPEN', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'EXPIRED');
CREATE TYPE reward_type AS ENUM ('MONEY', 'POINT');
CREATE TYPE order_status AS ENUM ('PENDING_ACCEPT', 'ACCEPTED', 'IN_PROGRESS', 'PENDING_CONFIRM', 'COMPLETED', 'CANCELLED');
CREATE TYPE reviewer_role AS ENUM ('PUBLISHER', 'ACCEPTOR');
CREATE TYPE arbitration_status AS ENUM ('PENDING', 'PROCESSING', 'RESOLVED', 'DISMISSED');
CREATE TYPE notification_type AS ENUM (
    'ORDER_ACCEPTED',
    'ORDER_COMPLETED',
    'ORDER_CANCELLED',
    'REVIEW_RECEIVED',
    'CREDIT_CHANGED',
    'SYSTEM_ANNOUNCEMENT',
    'ARBITRATION_UPDATE'
);
CREATE TYPE message_type AS ENUM ('TEXT', 'IMAGE', 'SYSTEM');

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    phone_cipher TEXT,
    phone_hash VARCHAR(128),
    email VARCHAR(200),
    student_id VARCHAR(50) NOT NULL UNIQUE,
    real_name VARCHAR(200) NOT NULL,
    avatar_url TEXT,
    credit_score INT NOT NULL DEFAULT 100 CHECK (credit_score >= 0),
    role user_role NOT NULL DEFAULT 'STUDENT',
    status user_status NOT NULL DEFAULT 'PENDING_CERT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE certifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    cert_type cert_type NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    card_image_url TEXT,
    status cert_status NOT NULL DEFAULT 'PENDING',
    reject_reason TEXT,
    reviewed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_cert_card_image CHECK (cert_type <> 'STUDENT_CARD' OR card_image_url IS NOT NULL)
);

CREATE TABLE credit_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    change_amount INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    related_order_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE demands (
    id BIGSERIAL PRIMARY KEY,
    publisher_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category demand_category NOT NULL,
    reward_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (reward_amount >= 0),
    reward_type reward_type NOT NULL DEFAULT 'MONEY',
    status demand_status NOT NULL DEFAULT 'OPEN',
    is_urgent BOOLEAN NOT NULL DEFAULT false,
    urgent_deadline TIMESTAMPTZ,
    location_tag VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_urgent_deadline CHECK (is_urgent = false OR urgent_deadline IS NOT NULL)
);

CREATE TABLE demand_images (
    id BIGSERIAL PRIMARY KEY,
    demand_id BIGINT NOT NULL REFERENCES demands(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    UNIQUE (demand_id, sort_order)
);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE demand_tags (
    demand_id BIGINT NOT NULL REFERENCES demands(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (demand_id, tag_id)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    demand_id BIGINT NOT NULL UNIQUE REFERENCES demands(id) ON DELETE RESTRICT,
    publisher_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    acceptor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status order_status NOT NULL DEFAULT 'ACCEPTED',
    accepted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    cancel_reason TEXT,
    auto_confirm_deadline TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_order_not_self_accept CHECK (publisher_id <> acceptor_id)
);

ALTER TABLE credit_records
    ADD CONSTRAINT fk_credit_record_order
    FOREIGN KEY (related_order_id) REFERENCES orders(id) ON DELETE SET NULL;

CREATE TABLE order_status_logs (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    from_status order_status,
    to_status order_status NOT NULL,
    operator_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    remark TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    reviewee_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    reviewer_role reviewer_role NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content TEXT,
    image_urls JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (order_id, reviewer_id),
    UNIQUE (order_id, reviewer_role),
    CONSTRAINT chk_review_not_self CHECK (reviewer_id <> reviewee_id),
    CONSTRAINT chk_low_rating_evidence CHECK (
        rating > 3 OR (content IS NOT NULL AND length(content) >= 15 AND jsonb_array_length(image_urls) >= 1)
    )
);

CREATE TABLE arbitration_tickets (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT REFERENCES reviews(id) ON DELETE SET NULL,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    reporter_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    reason TEXT NOT NULL,
    evidence_urls JSONB NOT NULL DEFAULT '[]'::jsonb,
    status arbitration_status NOT NULL DEFAULT 'PENDING',
    resolution TEXT,
    resolved_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    related_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user2_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    related_demand_id BIGINT REFERENCES demands(id) ON DELETE SET NULL,
    last_message_content VARCHAR(1000),
    last_message_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_conversation_distinct_users CHECK (user1_id <> user2_id)
);

CREATE UNIQUE INDEX uq_conversation_pair_demand
    ON conversations (
        LEAST(user1_id, user2_id),
        GREATEST(user1_id, user2_id),
        COALESCE(related_demand_id, 0)
    );

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    message_type message_type NOT NULL DEFAULT 'TEXT',
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE admin_operation_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    operation VARCHAR(200) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id BIGINT,
    detail TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 索引设计
-- 1. 用户登录与认证：username/student_id 已由 UNIQUE 自动建索引；phone_hash 用于手机号精确查找且不暴露明文。
CREATE INDEX idx_users_phone_hash ON users(phone_hash);
CREATE INDEX idx_users_status ON users(status);

-- 2. 需求浏览是高频入口，按状态、板块、位置、发布时间/报酬筛选排序。
CREATE INDEX idx_demands_status_created ON demands(status, created_at DESC);
CREATE INDEX idx_demands_category_status ON demands(category, status);
CREATE INDEX idx_demands_location_status ON demands(location_tag, status);
CREATE INDEX idx_demands_publisher_created ON demands(publisher_id, created_at DESC);
CREATE INDEX idx_demands_urgent_deadline ON demands(is_urgent, urgent_deadline);
CREATE INDEX idx_demands_keyword ON demands USING GIN (to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(description, '')));

-- 3. 多对多标签查询：主键覆盖 demand_id -> tag_id，补充 tag_id -> demand_id 方向。
CREATE INDEX idx_demand_tags_tag ON demand_tags(tag_id, demand_id);

-- 4. 订单列表按参与人和状态筛选。
CREATE INDEX idx_orders_publisher_status ON orders(publisher_id, status, updated_at DESC);
CREATE INDEX idx_orders_acceptor_status ON orders(acceptor_id, status, updated_at DESC);
CREATE INDEX idx_orders_auto_confirm ON orders(status, auto_confirm_deadline);
CREATE INDEX idx_order_status_logs_order_created ON order_status_logs(order_id, created_at DESC);

-- 5. 评价、信用、通知、会话消息均按用户或业务对象倒序读取。
CREATE INDEX idx_reviews_reviewee_created ON reviews(reviewee_id, created_at DESC);
CREATE INDEX idx_credit_records_user_created ON credit_records(user_id, created_at DESC);
CREATE INDEX idx_notifications_user_read_created ON notifications(user_id, is_read, created_at DESC);
CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at DESC);
CREATE INDEX idx_arbitration_status_created ON arbitration_tickets(status, created_at DESC);
CREATE INDEX idx_admin_logs_admin_created ON admin_operation_logs(admin_id, created_at DESC);

-- 隐私与安全说明
-- 1. 密码只存 password_hash，推荐应用层使用 BCrypt/Argon2id 加盐哈希，数据库不保存明文密码。
-- 2. 手机号属于敏感个人信息：phone_cipher 保存应用层字段级加密后的密文；phone_hash 保存带 pepper 的不可逆哈希用于查重/检索；页面展示时只返回脱敏值。
-- 3. 学生证照片、仲裁证据等文件存对象存储，数据库仅保存受权限控制的 URL 或对象 key。
-- 4. 订单详情中双方手机号是否可见由应用层基于订单状态和参与身份控制，数据库层不保存额外明文字段。

-- 范式说明
-- 表结构遵循第三范式：用户、需求、订单、评价、通知等主题独立成表；需求与标签的多对多通过 demand_tags 拆分；
-- 状态变化拆为 order_status_logs，避免在 orders 中存储重复历史字段。
