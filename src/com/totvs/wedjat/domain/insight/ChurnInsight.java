package com.totvs.wedjat.domain.insight;

import com.totvs.wedjat.domain.enums.InsightType;

public class ChurnInsight extends Insight {

    public ChurnInsight(String description, String evidence) {
        super(description, evidence);
    }

    @Override
    public InsightType getType() {
        return InsightType.CHURN;
    }

    @Override
    public String getPrioridade() {
        return "ALTA";
    }
}
