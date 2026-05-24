package com.totvs.wedjat.domain.insight;

import com.totvs.wedjat.domain.enums.InsightType;

public final class InsightFactory {

    private InsightFactory() {
    }

    public static Insight criar(InsightType type, String description, String evidence) {
        if (type == null) {
            throw new IllegalArgumentException("Tipo de insight obrigatório.");
        }

        return switch (type) {
            case RISCO -> new RiskInsight(description, evidence);
            case OPORTUNIDADE -> new OpportunityInsight(description, evidence);
            case CHURN -> new ChurnInsight(description, evidence);
            case CONCORRENTE -> new CompetitorInsight(description, evidence);
            case CROSS_SELL -> new CrossSellInsight(description, evidence);
        };
    }
}
