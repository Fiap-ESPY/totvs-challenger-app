package com.totvs.wedjat.domain.insight;

import com.totvs.wedjat.domain.enums.InsightType;

public class CompetitorInsight extends Insight {

    public CompetitorInsight(String description, String evidence) {
        super(description, evidence);
    }

    public CompetitorInsight(Long id, String description, String evidence) {
        super(id, description, evidence);
    }

    @Override
    public InsightType getType() {
        return InsightType.CONCORRENTE;
    }

    @Override
    public String getPrioridade() {
        return "MEDIA";
    }
}
