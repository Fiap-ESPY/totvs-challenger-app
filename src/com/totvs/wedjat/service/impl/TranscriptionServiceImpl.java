package com.totvs.wedjat.service.impl;

import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.SpicedAssessment;
import com.totvs.wedjat.domain.enums.InsightType;
import com.totvs.wedjat.domain.insight.Insight;
import com.totvs.wedjat.domain.insight.InsightFactory;
import com.totvs.wedjat.infrastructure.db.dao.MeetingRecordDAO;
import com.totvs.wedjat.infrastructure.db.dao.SpicedAssessmentDAO;
import com.totvs.wedjat.service.ITranscriptionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TranscriptionServiceImpl implements ITranscriptionService {

    private final MeetingRecordDAO  meetingDAO = new MeetingRecordDAO();
    private final SpicedAssessmentDAO spicedDAO = new SpicedAssessmentDAO();

    @Override
    public MeetingRecord registrarReuniao(Opportunity opportunity, String transcription) {
        MeetingRecord meeting = new MeetingRecord(LocalDate.now(), transcription);
        meeting.addInsights(analisarTranscricao(transcription));

        MeetingRecord salvo = meetingDAO.insert(meeting, opportunity.getId());

        aplicarSpicedDaTranscricao(opportunity, transcription);
        spicedDAO.update(opportunity.getSpicedAssessment());

        opportunity.addMeeting(salvo);
        return salvo;
    }

    @Override
    public List<Insight> analisarTranscricao(String transcription) {
        String texto = transcription.toLowerCase(Locale.ROOT);
        List<Insight> insights = new ArrayList<>();

        if (containsAny(texto, "protheus", "rm", "fluig", "rd station", "techfin")) {
            insights.add(InsightFactory.criar(InsightType.OPORTUNIDADE,
                    "Produto TOTVS ou módulo correlato identificado na conversa.", "transcrição"));
        }
        if (containsAny(texto, "senior", "sap", "oracle", "concorrente", "concorrência")) {
            insights.add(InsightFactory.criar(InsightType.CONCORRENTE,
                    "Menção a concorrente ou comparação competitiva.", "transcrição"));
        }
        if (containsAny(texto, "cancelar", "churn", "insatisf", "não renovar", "nao renovar")) {
            insights.add(InsightFactory.criar(InsightType.CHURN,
                    "Sinal de risco de churn ou insatisfação.", "transcrição"));
        }
        if (containsAny(texto, "orçamento", "cfo", "aprovação", "aprovacao", "bloqueio")) {
            insights.add(InsightFactory.criar(InsightType.RISCO,
                    "Possível bloqueio comercial (orçamento, CFO ou aprovação).", "transcrição"));
        }
        if (containsAny(texto, "cross-sell", "cross sell", "upsell", "up-sell",
                "módulo adicional", "modulo adicional")) {
            insights.add(InsightFactory.criar(InsightType.CROSS_SELL,
                    "Oportunidade de expansão (cross-sell/upsell).", "transcrição"));
        }
        if (containsAny(texto, "dor", "problema", "retrabalho", "manual")) {
            insights.add(InsightFactory.criar(InsightType.RISCO,
                    "Dor operacional identificada — validar impacto de negócio.", "transcrição"));
        }
        return insights;
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private void aplicarSpicedDaTranscricao(Opportunity opportunity, String transcription) {
        String texto = transcription.toLowerCase(Locale.ROOT);
        SpicedAssessment spiced = opportunity.getSpicedAssessment();

        if (spiced.getSituation() == null
                && containsAny(texto, "utiliza", "usa", "já tem", "ja tem", "ambiente")) {
            spiced.setSituation("Contexto extraído da transcrição: cliente já possui sistemas em uso.");
        }
        if (spiced.getPain() == null
                && containsAny(texto, "dor", "problema", "retrabalho", "manual", "integração", "integracao")) {
            spiced.setPain("Dor identificada na transcrição (processos, integração ou retrabalho).");
        }
        if (spiced.getImpact() == null
                && containsAny(texto, "custo", "perda", "produtividade", "compliance", "multa")) {
            spiced.setImpact("Impacto de negócio sugerido na transcrição.");
        }
        if (spiced.getCriticalEvent() == null
                && containsAny(texto, "prazo", "deadline", "até", "ate", "urgente", "janeiro", "dezembro")) {
            spiced.setCriticalEvent("Evento crítico com prazo mencionado na transcrição.");
        }
        if (spiced.getDecision() == null
                && containsAny(texto, "cfo", "diretor", "gerente", "comitê", "comite", "aprovação", "aprovacao")) {
            spiced.setDecision("Stakeholders/decisores mencionados na transcrição.");
        }
    }

    private boolean containsAny(String texto, String... termos) {
        for (String termo : termos) {
            if (texto.contains(termo)) return true;
        }
        return false;
    }
}
