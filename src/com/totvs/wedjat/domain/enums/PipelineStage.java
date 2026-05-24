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
        return switch (valor) {
            case "1", "GERACAO_DEMANDA" -> GERACAO_DEMANDA;
            case "2", "QUALIFICACAO" -> QUALIFICACAO;
            case "3", "PRE_VENDAS" -> PRE_VENDAS;
            case "4", "REUNIAO_DIAGNOSTICA" -> REUNIAO_DIAGNOSTICA;
            case "5", "PROPOSTA" -> PROPOSTA;
            case "6", "NEGOCIACAO" -> NEGOCIACAO;
            case "7", "FECHAMENTO" -> FECHAMENTO;
            default -> throw new IllegalArgumentException("Etapa inválida. Use 1 a 7 conforme o menu.");
        };
    }
}
