package com.totvs.wedjat.domain.insight;

import com.totvs.wedjat.domain.enums.InsightType;

public class RiskInsight extends Insight {

    public RiskInsight(String description, String evidence) {
        super(description, evidence);
    }

    @Override
    public InsightType getType() {
        return InsightType.RISCO;
    }

    @Override
    public String getPrioridade() {
        return "ALTA";
    }
}
