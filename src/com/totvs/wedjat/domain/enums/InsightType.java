package com.totvs.wedjat.domain.enums;

public enum InsightType {
    OPORTUNIDADE("Oportunidade"),
    RISCO("Risco"),
    CONCORRENTE("Concorrente"),
    CHURN("Churn"),
    CROSS_SELL("Cross-sell");

    private final String descricao;

    InsightType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return this.descricao;
    }
}
