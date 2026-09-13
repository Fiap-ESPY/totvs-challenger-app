package com.totvs.wedjat.domain;

/**
 * Representa a estrutura SPICED de uma oportunidade comercial
 * (Situation, Pain, Impact, Critical Event, Decision).
 * Permite avaliar completude dos campos e identificar lacunas, como gap de Impact.
 */
public class SpicedAssessment {

    private Long id;
    private String situation;
    private String pain;
    private String impact;
    private String criticalEvent;
    private String decision;

    /** Construtor para nova avaliação SPICED (ID atribuído pelo DAO via SEQUENCE). */
    public SpicedAssessment() {
        this(null, null, null, null, null, null);
    }

    /** Construtor para nova avaliação com campos pré-preenchidos. */
    public SpicedAssessment(
            String situation,
            String pain,
            String impact,
            String criticalEvent,
            String decision) {
        this(null, situation, pain, impact, criticalEvent, decision);
    }

    /** Construtor completo — usado pelo DAO ao carregar registros do banco. */
    public SpicedAssessment(
            Long id,
            String situation,
            String pain,
            String impact,
            String criticalEvent,
            String decision) {
        this.id = id;
        this.situation = normalize(situation);
        this.pain = normalize(pain);
        this.impact = normalize(impact);
        this.criticalEvent = normalize(criticalEvent);
        this.decision = normalize(decision);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() {
        return this.id;
    }


    public String getSituation() {
        return this.situation;
    }

    public void setSituation(String situation) {
        this.situation = normalize(situation);
    }

    public String getPain() {
        return this.pain;
    }

    public void setPain(String pain) {
        this.pain = normalize(pain);
    }

    public String getImpact() {
        return this.impact;
    }

    public void setImpact(String impact) {
        this.impact = normalize(impact);
    }

    public String getCriticalEvent() {
        return this.criticalEvent;
    }

    public void setCriticalEvent(String criticalEvent) {
        this.criticalEvent = normalize(criticalEvent);
    }

    public String getDecision() {
        return this.decision;
    }

    public void setDecision(String decision) {
        this.decision = normalize(decision);
    }

    public int countPreenchidos() {
        int count = 0;
        if (this.situation != null) count++;
        if (this.pain != null) count++;
        if (this.impact != null) count++;
        if (this.criticalEvent != null) count++;
        if (this.decision != null) count++;
        return count;
    }

    public boolean hasImpactGap() {
        return this.pain != null && this.impact == null;
    }
}
