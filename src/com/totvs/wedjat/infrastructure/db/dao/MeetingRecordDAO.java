package com.totvs.wedjat.infrastructure.db.dao;

import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.insight.Insight;
import com.totvs.wedjat.infrastructure.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MeetingRecordDAO {

    private final InsightDAO insightDAO = new InsightDAO();

    public MeetingRecord insert(MeetingRecord meeting, long opportunityId) {
        String sqlNext = "SELECT SQ_MEETING.NEXTVAL FROM DUAL";
        String sqlInsert =
                "INSERT INTO TB_MEETING_RECORD (id, id_opportunity, meeting_date, transcription) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmtNext = conn.prepareStatement(sqlNext);
             ResultSet rs = stmtNext.executeQuery()) {

            rs.next();
            long id = rs.getLong(1);

            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                stmtInsert.setLong(1, id);
                stmtInsert.setLong(2, opportunityId);
                stmtInsert.setDate(3, Date.valueOf(meeting.getDate()));
                stmtInsert.setString(4, meeting.getTranscription());
                stmtInsert.executeUpdate();
            }

            MeetingRecord saved = new MeetingRecord(id, meeting.getDate(), meeting.getTranscription());

            List<Insight> savedInsights = new ArrayList<>();
            for (Insight insight : meeting.getInsights()) {
                savedInsights.add(insightDAO.insert(insight, id));
            }
            saved.addInsights(savedInsights);

            return saved;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir MeetingRecord.", e);
        }
    }

    public List<MeetingRecord> findByOpportunityId(long opportunityId) {
        String sql =
                "SELECT id, meeting_date, transcription " +
                "FROM TB_MEETING_RECORD WHERE id_opportunity = ? ORDER BY meeting_date";

        List<MeetingRecord> meetings = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, opportunityId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id           = rs.getLong("id");
                    LocalDate date    = rs.getDate("meeting_date").toLocalDate();
                    String transcript = rs.getString("transcription");

                    MeetingRecord meeting = new MeetingRecord(id, date, transcript);
                    meeting.addInsights(insightDAO.findByMeetingId(id));
                    meetings.add(meeting);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar reuniões da oportunidade " + opportunityId + ".", e);
        }
        return meetings;
    }
}
