package com.totvs.wedjat.domain.insight;

import com.totvs.wedjat.domain.enums.InsightType;

public class CrossSellInsight extends Insight {

    public CrossSellInsight(String description, String evidence) {
        super(description, evidence);
    }

    @Override
    public InsightType getType() {
        return InsightType.CROSS_SELL;
    }

    @Override
    public String getPrioridade() {
        return "BAIXA";
    }
}
