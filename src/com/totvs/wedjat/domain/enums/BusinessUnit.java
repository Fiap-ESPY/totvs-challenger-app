package com.totvs.wedjat.domain.enums;

public enum BusinessUnit {
    TOTVS_GESTAO("TOTVS Gestão"),
    RD_STATION("RD Station"),
    TECHFIN("TOTVS Techfin");

    private final String descricao;

    BusinessUnit(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public static BusinessUnit fromInput(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("BU obrigatória.");
        }
        String valor = input.trim().toUpperCase();
        if ("1".equals(valor) || "TOTVS_GESTAO".equals(valor) || "GESTAO".equals(valor) || "TOTVS GESTAO".equals(valor)) {
            return TOTVS_GESTAO;
        }
        if ("2".equals(valor) || "RD_STATION".equals(valor) || "RD".equals(valor) || "RD STATION".equals(valor)) {
            return RD_STATION;
        }
        if ("3".equals(valor) || "TECHFIN".equals(valor) || "TOTVS TECHFIN".equals(valor)) {
            return TECHFIN;
        }
        throw new IllegalArgumentException("BU inválida. Use: 1-TOTVS_GESTAO, 2-RD_STATION, 3-TECHFIN");
    }
}
