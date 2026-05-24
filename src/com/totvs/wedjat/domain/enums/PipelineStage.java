package com.totvs.wedjat.domain.enums;

public enum PipelineStage {
    GERACAO_DEMANDA("Geração de demanda"),
    QUALIFICACAO("Qualificação"),
    PRE_VENDAS("Pré-vendas"),
    REUNIAO_DIAGNOSTICA("Reunião diagnóstica"),
    PROPOSTA("Proposta"),
    NEGOCIACAO("Negociação"),
    FECHAMENTO("Fechamento");

    private final String descricao;

    PipelineStage(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public static PipelineStage fromInput(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Etapa do pipeline obrigatória.");
        }
        String valor = input.trim().toUpperCase();
        switch (valor) {
            case "1":
            case "GERACAO_DEMANDA":
                return GERACAO_DEMANDA;
            case "2":
            case "QUALIFICACAO":
                return QUALIFICACAO;
            case "3":
            case "PRE_VENDAS":
                return PRE_VENDAS;
            case "4":
            case "REUNIAO_DIAGNOSTICA":
                return REUNIAO_DIAGNOSTICA;
            case "5":
            case "PROPOSTA":
                return PROPOSTA;
            case "6":
            case "NEGOCIACAO":
                return NEGOCIACAO;
            case "7":
            case "FECHAMENTO":
                return FECHAMENTO;
            default:
                throw new IllegalArgumentException("Etapa inválida. Use 1 a 7 conforme o menu.");
        }
    }
}
