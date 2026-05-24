package com.totvs.wedjat.domain;

import java.util.Random;

/**
 * Representa o score de qualidade do handoff comercial (pré-vendas para vendas).
 * Consolida pontuação, classificação e vínculo com a oportunidade avaliada.
 */
public class HandoffScore {

    private final Long id = Math.abs(new Random().nextLong());
    private final int score;
    private final String classificacao;
    private final int camposSpicedPreenchidos;
    private final Opportunity opportunity;

    public HandoffScore(Opportunity opportunity, int score, String classificacao, int camposSpicedPreenchidos) {
        if (opportunity == null) {
            throw new IllegalArgumentException("Oportunidade obrigatória para o handoff score.");
        }
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score deve estar entre 0 e 100.");
        }
        if (classificacao == null || classificacao.isBlank()) {
            throw new IllegalArgumentException("Classificação obrigatória.");
        }
        this.opportunity = opportunity;
        this.score = score;
        this.classificacao = classificacao.trim();
        this.camposSpicedPreenchidos = camposSpicedPreenchidos;
    }

    public Long getId() {
        return this.id;
    }

    public int getScore() {
        return this.score;
    }

    public String getClassificacao() {
        return this.classificacao;
    }

    public int getCamposSpicedPreenchidos() {
        return this.camposSpicedPreenchidos;
    }

    public Opportunity getOpportunity() {
        return this.opportunity;
    }

    @Override
    public String toString() {
        String resumoFormatado =
                "================================%n"
                        + "   HANDOFF SCORE%n"
                        + "================================%n"
                        + "ID:                      %d%n"
                        + "Oportunidade vinculada:  #%d - %s%n"
                        + "Score:                   %d/100%n"
                        + "Classificacao:           %s%n"
                        + "SPICED preenchido:       %d/5 campos%n";

        return String.format(
                resumoFormatado,
                this.getId(),
                this.getOpportunity().getId(),
                this.getOpportunity().getClientName(),
                this.getScore(),
                this.getClassificacao(),
                this.getCamposSpicedPreenchidos());
    }
}
