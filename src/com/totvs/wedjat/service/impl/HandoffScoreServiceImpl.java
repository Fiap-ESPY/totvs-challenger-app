package com.totvs.wedjat.service.impl;

import com.totvs.wedjat.domain.HandoffScore;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.SpicedAssessment;
import com.totvs.wedjat.service.IHandoffScoreService;

public class HandoffScoreServiceImpl implements IHandoffScoreService {

    private static final int PESO_CLIENTE = 15;
    private static final int PESO_PRODUTO = 15;
    private static final int PESO_BU = 10;
    private static final int PESO_ETAPA = 10;
    private static final int PESO_SPICED = 50;

    @Override
    public int calcular(Opportunity opportunity) {
        int score = 0;

        if (opportunity.getClientName() != null && !opportunity.getClientName().isBlank()) {
            score += PESO_CLIENTE;
        }
        if (opportunity.getProduct() != null && !opportunity.getProduct().isBlank()) {
            score += PESO_PRODUTO;
        }
        if (opportunity.getBusinessUnit() != null) {
            score += PESO_BU;
        }
        if (opportunity.getPipelineStage() != null) {
            score += PESO_ETAPA;
        }

        SpicedAssessment spiced = opportunity.getSpicedAssessment();
        int camposSpiced = spiced.countPreenchidos();
        score += (camposSpiced * PESO_SPICED) / 5;

        return Math.min(score, 100);
    }

    @Override
    public String classificar(int score) {
        if (score >= 80) {
            return "Handoff de alta qualidade";
        }
        if (score >= 50) {
            return "Handoff parcial — revisar lacunas antes da reunião";
        }
        return "Handoff insuficiente — risco de reunião genérica";
    }

    @Override
    public HandoffScore gerarHandoffScore(Opportunity opportunity) {
        int score = this.calcular(opportunity);
        String classificacao = this.classificar(score);
        int camposSpiced = opportunity.getSpicedAssessment().countPreenchidos();
        return new HandoffScore(opportunity, score, classificacao, camposSpiced);
    }

    @Override
    public String gerarRelatorio(Opportunity opportunity) {
        HandoffScore handoffScore = this.gerarHandoffScore(opportunity);
        opportunity.setHandoffScore(handoffScore);
        return handoffScore.toString();
    }
}
