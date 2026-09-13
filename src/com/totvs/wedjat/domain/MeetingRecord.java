package com.totvs.wedjat.domain;

import com.totvs.wedjat.domain.insight.Insight;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa o registro de uma reunião comercial vinculada a uma oportunidade.
 * Armazena data, transcrição e os insights extraídos automaticamente da conversa.
 */
public class MeetingRecord {

    private Long id;
    private final LocalDate date;
    private final String transcription;
    private final List<Insight> insights = new ArrayList<>();

    /** Construtor para nova reunião (ID atribuído pelo DAO via SEQUENCE). */
    public MeetingRecord(LocalDate date, String transcription) {
        this(null, date, transcription);
    }

    /** Construtor completo — usado pelo DAO ao carregar registros do banco. */
    public MeetingRecord(Long id, LocalDate date, String transcription) {
        if (date == null) {
            throw new IllegalArgumentException("Data da reunião obrigatória.");
        }
        if (transcription == null || transcription.isBlank()) {
            throw new IllegalArgumentException("Transcrição obrigatória.");
        }
        this.id = id;
        this.date = date;
        this.transcription = transcription.trim();
    }

    public Long getId() {
        return this.id;
    }


    public LocalDate getDate() {
        return this.date;
    }

    public String getTranscription() {
        return this.transcription;
    }

    public List<Insight> getInsights() {
        return Collections.unmodifiableList(this.insights);
    }

    public void addInsight(Insight insight) {
        if (insight != null) {
            this.insights.add(insight);
        }
    }

    public void addInsights(List<Insight> novosInsights) {
        if (novosInsights != null) {
            for (Insight insight : novosInsights) {
                this.addInsight(insight);
            }
        }
    }

    @Override
    public String toString() {
        String resumoTranscricao = this.resumirTranscricao(this.getTranscription());
        String resumoFormatado =
                "================================%n"
                        + "   REUNIAO%n"
                        + "================================%n"
                        + "ID:                      %d%n"
                        + "Data:                    %s%n"
                        + "Insights identificados:  %d%n"
                        + "Transcricao (resumo):    %s%n";

        return String.format(
                resumoFormatado,
                this.id,
                this.date,
                this.insights.size(),
                resumoTranscricao);
    }

    private String resumirTranscricao(String texto) {
        if (texto == null || texto.isBlank()) {
            return "sem transcricao";
        }
        String valor = texto.trim();
        int limite = 80;
        if (valor.length() <= limite) {
            return valor;
        }
        return valor.substring(0, limite) + "...";
    }
}
