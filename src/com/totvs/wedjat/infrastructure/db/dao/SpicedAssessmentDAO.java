package com.totvs.wedjat.infrastructure.db.dao;

import com.totvs.wedjat.domain.SpicedAssessment;
import com.totvs.wedjat.infrastructure.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SpicedAssessmentDAO {

    public SpicedAssessment insert(SpicedAssessment spiced, long opportunityId) {
        String sqlNext = "SELECT SQ_SPICED.NEXTVAL FROM DUAL";
        String sqlInsert =
                "INSERT INTO TB_SPICED_ASSESSMENT " +
                "(id, id_opportunity, situation, pain, impact, critical_event, decision) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmtNext = conn.prepareStatement(sqlNext);
             ResultSet rs = stmtNext.executeQuery()) {

            rs.next();
            long id = rs.getLong(1);

            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                stmtInsert.setLong(1, id);
                stmtInsert.setLong(2, opportunityId);
                stmtInsert.setString(3, spiced.getSituation());
                stmtInsert.setString(4, spiced.getPain());
                stmtInsert.setString(5, spiced.getImpact());
                stmtInsert.setString(6, spiced.getCriticalEvent());
                stmtInsert.setString(7, spiced.getDecision());
                stmtInsert.executeUpdate();
            }

            return new SpicedAssessment(
                    id,
                    spiced.getSituation(),
                    spiced.getPain(),
                    spiced.getImpact(),
                    spiced.getCriticalEvent(),
                    spiced.getDecision());

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir SpicedAssessment.", e);
        }
    }

    public void update(SpicedAssessment spiced) {
        String sql =
                "UPDATE TB_SPICED_ASSESSMENT " +
                "SET situation = ?, pain = ?, impact = ?, critical_event = ?, decision = ? " +
                "WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, spiced.getSituation());
            stmt.setString(2, spiced.getPain());
            stmt.setString(3, spiced.getImpact());
            stmt.setString(4, spiced.getCriticalEvent());
            stmt.setString(5, spiced.getDecision());
            stmt.setLong(6, spiced.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar SpicedAssessment id=" + spiced.getId() + ".", e);
        }
    }

    public Optional<SpicedAssessment> findByOpportunityId(long opportunityId) {
        String sql =
                "SELECT id, situation, pain, impact, critical_event, decision " +
                "FROM TB_SPICED_ASSESSMENT WHERE id_opportunity = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, opportunityId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new SpicedAssessment(
                            rs.getLong("id"),
                            rs.getString("situation"),
                            rs.getString("pain"),
                            rs.getString("impact"),
                            rs.getString("critical_event"),
                            rs.getString("decision")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar SpicedAssessment da oportunidade " + opportunityId + ".", e);
        }
        return Optional.empty();
    }
}
