package com.totvs.wedjat.infrastructure.db.dao;

import com.totvs.wedjat.domain.HandoffScore;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.infrastructure.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class HandoffScoreDAO {

    public HandoffScore insertOrUpdate(HandoffScore handoff, long opportunityId) {
        return exists(opportunityId) ? update(handoff, opportunityId) : insert(handoff, opportunityId);
    }

    private boolean exists(long opportunityId) {
        String sql = "SELECT COUNT(1) FROM TB_HANDOFF_SCORE WHERE id_opportunity = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, opportunityId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar existência do HandoffScore.", e);
        }
    }

    private HandoffScore insert(HandoffScore handoff, long opportunityId) {
        String sqlNext   = "SELECT SQ_HANDOFF.NEXTVAL FROM DUAL";
        String sqlInsert =
                "INSERT INTO TB_HANDOFF_SCORE (id, id_opportunity, score, classificacao, campos_spiced) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmtNext = conn.prepareStatement(sqlNext);
             ResultSet rs = stmtNext.executeQuery()) {

            rs.next();
            long id = rs.getLong(1);

            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                stmtInsert.setLong(1, id);
                stmtInsert.setLong(2, opportunityId);
                stmtInsert.setInt(3, handoff.getScore());
                stmtInsert.setString(4, handoff.getClassificacao());
                stmtInsert.setInt(5, handoff.getCamposSpicedPreenchidos());
                stmtInsert.executeUpdate();
            }

            return new HandoffScore(id, handoff.getOpportunity(), handoff.getScore(),
                    handoff.getClassificacao(), handoff.getCamposSpicedPreenchidos());

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir HandoffScore.", e);
        }
    }

    private HandoffScore update(HandoffScore handoff, long opportunityId) {
        String sql =
                "UPDATE TB_HANDOFF_SCORE " +
                "SET score = ?, classificacao = ?, campos_spiced = ? " +
                "WHERE id_opportunity = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, handoff.getScore());
            stmt.setString(2, handoff.getClassificacao());
            stmt.setInt(3, handoff.getCamposSpicedPreenchidos());
            stmt.setLong(4, opportunityId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar HandoffScore da oportunidade " + opportunityId + ".", e);
        }
        return handoff;
    }

    public Optional<HandoffScore> findByOpportunityId(Opportunity opportunity) {
        String sql =
                "SELECT id, score, classificacao, campos_spiced " +
                "FROM TB_HANDOFF_SCORE WHERE id_opportunity = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, opportunity.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new HandoffScore(
                            rs.getLong("id"),
                            opportunity,
                            rs.getInt("score"),
                            rs.getString("classificacao"),
                            rs.getInt("campos_spiced")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar HandoffScore da oportunidade " + opportunity.getId() + ".", e);
        }
        return Optional.empty();
    }
}
