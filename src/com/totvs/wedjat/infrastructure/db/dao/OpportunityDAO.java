package com.totvs.wedjat.infrastructure.db.dao;

import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.SpicedAssessment;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.infrastructure.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OpportunityDAO {

    private final SpicedAssessmentDAO spicedDAO    = new SpicedAssessmentDAO();
    private final MeetingRecordDAO    meetingDAO   = new MeetingRecordDAO();

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    public Opportunity insert(Opportunity opportunity) {
        String sqlNext   = "SELECT SQ_OPPORTUNITY.NEXTVAL FROM DUAL";
        String sqlInsert =
                "INSERT INTO TB_OPPORTUNITY (id, client_name, product, business_unit, pipeline_stage) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmtNext = conn.prepareStatement(sqlNext);
             ResultSet rs = stmtNext.executeQuery()) {

            rs.next();
            long id = rs.getLong(1);

            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                stmtInsert.setLong(1, id);
                stmtInsert.setString(2, opportunity.getClientName());
                stmtInsert.setString(3, opportunity.getProduct());
                stmtInsert.setString(4, opportunity.getBusinessUnit().name());
                stmtInsert.setString(5, opportunity.getPipelineStage().name());
                stmtInsert.executeUpdate();
            }

            Opportunity saved = new Opportunity(id,
                    opportunity.getClientName(),
                    opportunity.getProduct(),
                    opportunity.getBusinessUnit(),
                    opportunity.getPipelineStage());

            SpicedAssessment savedSpiced = spicedDAO.insert(opportunity.getSpicedAssessment(), id);
            saved.setSpicedAssessment(savedSpiced);

            return saved;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir oportunidade.", e);
        }
    }

    // -------------------------------------------------------------------------
    // FIND ALL
    // -------------------------------------------------------------------------

    public List<Opportunity> findAll() {
        String sql =
                "SELECT id, client_name, product, business_unit, pipeline_stage " +
                "FROM TB_OPPORTUNITY ORDER BY id";

        List<Opportunity> result = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar oportunidades.", e);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // FIND BY ID
    // -------------------------------------------------------------------------

    public Optional<Opportunity> findById(long id) {
        String sql =
                "SELECT id, client_name, product, business_unit, pipeline_stage " +
                "FROM TB_OPPORTUNITY WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar oportunidade id=" + id + ".", e);
        }
        return Optional.empty();
    }

    // -------------------------------------------------------------------------
    // UPDATE STAGE
    // -------------------------------------------------------------------------

    public boolean updateStage(long id, PipelineStage novaEtapa) {
        String sql = "UPDATE TB_OPPORTUNITY SET pipeline_stage = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaEtapa.name());
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar etapa da oportunidade id=" + id + ".", e);
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public boolean delete(long id) {
        String sql = "DELETE FROM TB_OPPORTUNITY WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar oportunidade id=" + id + ".", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Opportunity mapRow(ResultSet rs) throws SQLException {
        long id                 = rs.getLong("id");
        String clientName       = rs.getString("client_name");
        String product          = rs.getString("product");
        BusinessUnit bu         = BusinessUnit.valueOf(rs.getString("business_unit"));
        PipelineStage stage     = PipelineStage.valueOf(rs.getString("pipeline_stage"));

        Opportunity opp = new Opportunity(id, clientName, product, bu, stage);

        spicedDAO.findByOpportunityId(id).ifPresent(opp::setSpicedAssessment);

        List<MeetingRecord> meetings = meetingDAO.findByOpportunityId(id);
        for (MeetingRecord m : meetings) {
            opp.addMeeting(m);
        }

        return opp;
    }

}
