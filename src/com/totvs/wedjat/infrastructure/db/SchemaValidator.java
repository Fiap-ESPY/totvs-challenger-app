package com.totvs.wedjat.infrastructure.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifica se sequences e tabelas esperadas existem no schema Oracle do usuário conectado.
 */
public final class SchemaValidator {

    private static final String[] SEQUENCES = {
            "SQ_OPPORTUNITY",
            "SQ_SPICED",
            "SQ_MEETING",
            "SQ_INSIGHT",
            "SQ_HANDOFF"
    };

    private static final String[] TABLES = {
            "TB_OPPORTUNITY",
            "TB_SPICED_ASSESSMENT",
            "TB_MEETING_RECORD",
            "TB_INSIGHT",
            "TB_HANDOFF_SCORE"
    };

    private SchemaValidator() {
    }

    public static List<String> findMissingObjects() {
        List<String> missing = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection()) {
            for (String seq : SEQUENCES) {
                if (!existsSequence(conn, seq)) {
                    missing.add("SEQUENCE " + seq);
                }
            }
            for (String table : TABLES) {
                if (!existsTable(conn, table)) {
                    missing.add("TABLE " + table);
                }
            }
        } catch (SQLException e) {
            missing.add("CONEXÃO: " + e.getMessage());
        }
        return missing;
    }

    private static boolean existsSequence(Connection conn, String name) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_sequences WHERE sequence_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static boolean existsTable(Connection conn, String name) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tables WHERE table_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
