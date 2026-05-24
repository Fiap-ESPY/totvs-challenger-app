package com.totvs.wedjat.domain;

import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.domain.insight.ChurnInsight;
import com.totvs.wedjat.domain.insight.Insight;
import com.totvs.wedjat.domain.insight.RiskInsight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Representa uma oportunidade comercial monitorada pelo Wedjat.
 * Centraliza dados do cliente, produto, unidade de negócio (BU), etapa do pipeline,
 * avaliação SPICED e histórico de reuniões com insights associados.
 */
public class Opportunity {

    private final Long id = Math.abs(new Random().nextLong());
    private final String clientName;
    private final String product;
    private final BusinessUnit businessUnit;
    private PipelineStage pipelineStage;
    private final SpicedAssessment spicedAssessment;
    private final List<MeetingRecord> meetings = new ArrayList<>();
    private HandoffScore handoffScore;

    public Opportunity(
            String clientName,
            String product,
            BusinessUnit businessUnit,
            PipelineStage pipelineStage) {
        if (clientName == null || clientName.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente obrigatório.");
        }
        if (product == null || product.isBlank()) {
            throw new IllegalArgumentException("Produto obrigatório.");
        }
        if (businessUnit == null) {
            throw new IllegalArgumentException("BU obrigatória.");
        }
        if (pipelineStage == null) {
            throw new IllegalArgumentException("Etapa do pipeline obrigatória.");
        }

        this.clientName = clientName.trim();
        this.product = product.trim();
        this.businessUnit = businessUnit;
        this.pipelineStage = pipelineStage;
        this.spicedAssessment = new SpicedAssessment();
    }

    public Long getId() {
        return this.id;
    }

    public String getClientName() {
        return clientName;
    }

    public String getProduct() {
        return product;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public PipelineStage getPipelineStage() {
        return pipelineStage;
    }

    public void setPipelineStage(PipelineStage pipelineStage) {
        if (pipelineStage == null) {
            throw new IllegalArgumentException("Etapa do pipeline obrigatória.");
        }
        this.pipelineStage = pipelineStage;
    }

    public SpicedAssessment getSpicedAssessment() {
        return spicedAssessment;
    }

    public List<MeetingRecord> getMeetings() {
        return Collections.unmodifiableList(meetings);
    }

    public void addMeeting(MeetingRecord meeting) {
        if (meeting != null) {
            meetings.add(meeting);
        }
    }

    public HandoffScore getHandoffScore() {
        return this.handoffScore;
    }

    public void setHandoffScore(HandoffScore handoffScore) {
        this.handoffScore = handoffScore;
    }

    public boolean hasRisk() {
        if (spicedAssessment.hasImpactGap()) {
            return true;
        }
        for (MeetingRecord meeting : meetings) {
            for (Insight insight : meeting.getInsights()) {
                if (insight instanceof RiskInsight || insight instanceof ChurnInsight) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String riskLabel = this.hasRisk() ? "SIM" : "NAO";
        String detalhesHandoff = this.handoffScore != null
                ? this.handoffScore.toString()
                : "sem handoff score calculado";

        String resumoFormatado =
                "================================%n"
                        + "   OPORTUNIDADE%n"
                        + "================================%n"
                        + "ID:                      %d%n"
                        + "Cliente:                 %s%n"
                        + "Produto:                 %s%n"
                        + "BU:                      %s%n"
                        + "Etapa do pipeline:       %s%n"
                        + "SPICED preenchido:       %d/5 campos%n"
                        + "Reunioes registradas:    %d%n"
                        + "Risco identificado:      %s%n"
                        + "----------------------------%n"
                        + "Detalhes do handoff:%n"
                        + "%s%n";

        return String.format(
                resumoFormatado,
                this.id,
                this.clientName,
                this.product,
                this.businessUnit.getDescricao(),
                this.pipelineStage.getDescricao(),
                this.spicedAssessment.countPreenchidos(),
                this.meetings.size(),
                riskLabel,
                detalhesHandoff);
    }
}
