package com.example.backend.common;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaMigrationRunner implements CommandLineRunner {

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
                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    reviewed_by BIGINT,
                    reviewed_at TIMESTAMP,
                    resolution TEXT,
                    created_at TIMESTAMP
                )
                """);
    }

    private void executeIgnoreDuplicate(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (RuntimeException ignored) {
        }
    }
}
