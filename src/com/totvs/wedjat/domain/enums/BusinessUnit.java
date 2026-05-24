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
        return switch (valor) {
            case "1", "TOTVS_GESTAO", "GESTAO", "TOTVS GESTAO" -> TOTVS_GESTAO;
            case "2", "RD_STATION", "RD", "RD STATION" -> RD_STATION;
            case "3", "TECHFIN", "TOTVS TECHFIN" -> TECHFIN;
            default -> throw new IllegalArgumentException("BU inválida. Use: 1-TOTVS_GESTAO, 2-RD_STATION, 3-TECHFIN");
        };
    }
}
