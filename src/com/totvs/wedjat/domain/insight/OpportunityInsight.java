package com.totvs.wedjat.domain.insight;

import com.totvs.wedjat.domain.enums.InsightType;

public class OpportunityInsight extends Insight {

    public OpportunityInsight(String description, String evidence) {
        super(description, evidence);
    }

    public OpportunityInsight(Long id, String description, String evidence) {
        super(id, description, evidence);
    }

    @Override
    public InsightType getType() {
        return InsightType.OPORTUNIDADE;
    }

    @Override
    public String getPrioridade() {
        return "MEDIA";
    }
}
