package com.totvs.wedjat.domain.insight;

import com.totvs.wedjat.domain.enums.InsightType;

public final class InsightFactory {

    private InsightFactory() {
    }

    public static Insight criar(InsightType type, String description, String evidence) {
        if (type == null) {
            throw new IllegalArgumentException("Tipo de insight obrigatório.");
        }
        switch (type) {
            case RISCO:
                return new RiskInsight(description, evidence);
            case OPORTUNIDADE:
                return new OpportunityInsight(description, evidence);
            case CHURN:
                return new ChurnInsight(description, evidence);
            case CONCORRENTE:
                return new CompetitorInsight(description, evidence);
            case CROSS_SELL:
                return new CrossSellInsight(description, evidence);
            default:
                throw new IllegalArgumentException("Tipo de insight não suportado: " + type);
        }
    }
}
