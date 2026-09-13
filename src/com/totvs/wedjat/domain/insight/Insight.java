package com.totvs.wedjat.domain.insight;

import com.totvs.wedjat.domain.enums.InsightType;

/**
 * Classe abstrata que representa um achado da análise conversacional de uma reunião comercial.
 * Subclasses especializam o tipo de insight e a prioridade comercial associada.
 */
public abstract class Insight {

    private Long id;
    private final String description;
    private final String evidence;

    /** Construtor para novo insight (ID atribuído pelo DAO via SEQUENCE). */
    protected Insight(String description, String evidence) {
        this(null, description, evidence);
    }

    /** Construtor completo — usado pelo DAO ao carregar registros do banco. */
    protected Insight(Long id, String description, String evidence) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Descrição do insight obrigatória.");
        }
        this.id = id;
        this.description = description.trim();
        this.evidence = evidence == null || evidence.isBlank() ? "transcrição" : evidence.trim();
    }

    public Long getId() {
        return this.id;
    }


    public String getDescription() {
        return this.description;
    }

    public String getEvidence() {
        return this.evidence;
    }

    public abstract InsightType getType();

    public abstract String getPrioridade();

    @Override
    public String toString() {
        String resumoFormatado =
                "================================%n"
                        + "   INSIGHT%n"
                        + "================================%n"
                        + "ID:                      %d%n"
                        + "Tipo:                    %s%n"
                        + "Prioridade:              %s%n"
                        + "Descricao:               %s%n"
                        + "Evidencia:               %s%n";

        return String.format(
                resumoFormatado,
                this.getId(),
                this.getType().getDescricao(),
                this.getPrioridade(),
                this.getDescription(),
                this.getEvidence() == null || this.getEvidence().isBlank() ? "sem evidencia" : this.getEvidence());
    }
}
