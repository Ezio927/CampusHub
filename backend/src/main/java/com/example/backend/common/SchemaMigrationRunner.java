package com.example.backend.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        executeIgnoreDuplicate("ALTER TABLE conversations ADD COLUMN conversation_key VARCHAR(80)");
        executeIgnoreDuplicate("ALTER TABLE conversations ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'NORMAL'");
        executeIgnoreDuplicate("ALTER TABLE conversations ADD COLUMN blocked_by_user_id BIGINT");
        executeIgnoreDuplicate("ALTER TABLE conversations ADD COLUMN user1_last_read_message_id BIGINT DEFAULT 0");
        executeIgnoreDuplicate("ALTER TABLE conversations ADD COLUMN user2_last_read_message_id BIGINT DEFAULT 0");
        executeIgnoreDuplicate("CREATE INDEX idx_conversations_key ON conversations(conversation_key)");
        executeIgnoreDuplicate("ALTER TABLE messages ADD COLUMN quoted_message_id BIGINT");
        executeIgnoreDuplicate("ALTER TABLE messages ADD COLUMN quoted_content VARCHAR(500)");
        executeIgnoreDuplicate("ALTER TABLE messages ADD COLUMN client_message_id VARCHAR(80)");
        executeIgnoreDuplicate("ALTER TABLE messages ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SENT'");
        executeIgnoreDuplicate("ALTER TABLE messages MODIFY content MEDIUMTEXT");
        executeIgnoreDuplicate("CREATE INDEX idx_messages_conversation_id ON messages(conversation_id, id)");
        executeIgnoreDuplicate("CREATE UNIQUE INDEX uk_messages_sender_client ON messages(sender_id, client_message_id)");
        executeIgnoreDuplicate("ALTER TABLE certifications MODIFY card_image_url MEDIUMTEXT");
        executeIgnoreDuplicate("ALTER TABLE certifications ALTER COLUMN card_image_url CLOB");
        executeIgnoreDuplicate("ALTER TABLE users ADD COLUMN quota_available INT NOT NULL DEFAULT 0");
        executeIgnoreDuplicate("ALTER TABLE users ADD COLUMN quota_frozen INT NOT NULL DEFAULT 0");
        executeIgnoreDuplicate("ALTER TABLE credit_records ADD COLUMN type VARCHAR(30)");
        executeIgnoreDuplicate("ALTER TABLE credit_records ADD COLUMN before_score INT");
        executeIgnoreDuplicate("ALTER TABLE credit_records ADD COLUMN after_score INT");
        executeIgnoreDuplicate("ALTER TABLE credit_records ADD COLUMN operator_id BIGINT");
        executeIgnoreDuplicate("ALTER TABLE orders ADD COLUMN acceptor_deposit_amount INT NOT NULL DEFAULT 0");
        executeIgnoreDuplicate("ALTER TABLE orders ADD COLUMN active_demand_id BIGINT");
        executeIgnoreDuplicate("CREATE UNIQUE INDEX uk_orders_active_demand ON orders(active_demand_id)");
        executeIgnoreDuplicate("ALTER TABLE message_reports ADD COLUMN message_type VARCHAR(20)");
        executeIgnoreDuplicate("ALTER TABLE message_reports ADD COLUMN message_image_url MEDIUMTEXT");
        executeIgnoreDuplicate("ALTER TABLE message_reports ADD COLUMN message_image_url CLOB");
        jdbcTemplate.execute("""
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
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS credit_appeals (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    reason VARCHAR(500),
                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    reviewed_by BIGINT,
                    reviewed_at TIMESTAMP,
                    created_at TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
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
                )
                """);
        executeIgnoreDuplicate("CREATE INDEX idx_quota_records_user_id ON quota_records(user_id)");
        executeIgnoreDuplicate("CREATE INDEX idx_quota_records_demand ON quota_records(related_demand_id)");
        executeIgnoreDuplicate("CREATE INDEX idx_quota_records_order ON quota_records(related_order_id)");
    }

    private void executeIgnoreDuplicate(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (RuntimeException ex) {
            if (isExpectedMigrationError(ex)) {
                return;
            }
            log.warn("Migration failed, SQL: {} — {}", sql, ex.getMessage());
            throw ex;
        }
    }

    private boolean isExpectedMigrationError(Throwable ex) {
        if (ex == null) {
            return false;
        }
        String msg = ex.getMessage();
        if (msg != null) {
            String lower = msg.toLowerCase();
            if (lower.contains("duplicate")
                    || lower.contains("already exists")
                    || lower.contains("syntax error")
                    || lower.contains("bad sql grammar")) {
                return true;
            }
        }
        return isExpectedMigrationError(ex.getCause());
    }
}
