package com.totvs.wedjat.domain.insight;

import com.totvs.wedjat.domain.enums.InsightType;

public class ChurnInsight extends Insight {

    public ChurnInsight(String description, String evidence) {
        super(description, evidence);
    }

    public ChurnInsight(Long id, String description, String evidence) {
        super(id, description, evidence);
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
