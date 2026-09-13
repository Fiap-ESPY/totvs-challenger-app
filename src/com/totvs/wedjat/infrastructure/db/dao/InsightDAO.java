package com.totvs.wedjat.infrastructure.db.dao;

import com.totvs.wedjat.domain.enums.InsightType;
import com.totvs.wedjat.domain.insight.Insight;
import com.totvs.wedjat.domain.insight.InsightFactory;
import com.totvs.wedjat.infrastructure.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InsightDAO {

    public void insert(Insight insight, long meetingId) {
        String sqlNext = "SELECT SQ_INSIGHT.NEXTVAL FROM DUAL";
        String sqlInsert =
                "INSERT INTO TB_INSIGHT (id, id_meeting, type, description, evidence) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmtNext = conn.prepareStatement(sqlNext);
             ResultSet rs = stmtNext.executeQuery()) {

            rs.next();
            long id = rs.getLong(1);

            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                stmtInsert.setLong(1, id);
                stmtInsert.setLong(2, meetingId);
                stmtInsert.setString(3, insight.getType().name());
                stmtInsert.setString(4, insight.getDescription());
                stmtInsert.setString(5, insight.getEvidence());
                stmtInsert.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir insight.", e);
        }
    }

    public List<Insight> findByMeetingId(long meetingId) {
        String sql =
                "SELECT id, type, description, evidence " +
                "FROM TB_INSIGHT WHERE id_meeting = ?";

        List<Insight> insights = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, meetingId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id          = rs.getLong("id");
                    String typeName  = rs.getString("type");
                    String desc      = rs.getString("description");
                    String evidence  = rs.getString("evidence");

                    InsightType type = InsightType.valueOf(typeName);
                    insights.add(InsightFactory.carregar(id, type, desc, evidence));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar insights da reunião " + meetingId + ".", e);
        }
        return insights;
    }
}
