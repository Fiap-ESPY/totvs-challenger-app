package com.totvs.wedjat.console;

import com.totvs.wedjat.application.WedjatSystem;
import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.infrastructure.db.DbExceptionMessages;

import java.util.List;

public final class ConsoleMenuHandler {

    private final ConsoleInput input;
    private final WedjatSystem sistema;

    public ConsoleMenuHandler(ConsoleInput input, WedjatSystem sistema) {
        this.input = input;
        this.sistema = sistema;
    }

    public void cadastrarOportunidade() {
        try {
            String clientName = input.readRequiredLine("Nome do cliente: ");
            String product = input.readRequiredLine("Produto: ");
            this.exibirBusinessUnits();
            BusinessUnit bu = BusinessUnit.fromInput(input.readRequiredLine("BU (1-3): "));
            this.exibirPipelineStages();
            PipelineStage etapa = PipelineStage.fromInput(input.readRequiredLine("Etapa (1-7): "));

            Opportunity opportunity = sistema.cadastrarOportunidade(clientName, product, bu, etapa);
            System.out.println("Oportunidade cadastrada com sucesso!");
            System.out.println(opportunity);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(DbExceptionMessages.format(e));
        }
    }

    public void listarOportunidades() {
        try {
            listarOportunidadesInterno();
        } catch (RuntimeException e) {
            System.out.println(DbExceptionMessages.format(e));
        }
    }

    private void listarOportunidadesInterno() {
        List<Opportunity> oportunidades = sistema.listarOportunidades();
        if (oportunidades.isEmpty()) {
            System.out.println("Nenhuma oportunidade cadastrada.");
            return;
        }
        System.out.println("========= Oportunidades =========");
        for (Opportunity o : oportunidades) {
            System.out.println(o);
        }
    }

    public void registrarReuniao() {
        long id = input.readLong("ID da oportunidade: ");
        String transcription = input.readRequiredLine("Cole a transcrição da reunião: ");
        try {
            MeetingRecord meeting = sistema.registrarReuniao(id, transcription);
            System.out.println(meeting);
            System.out.println("Insights identificados:");
            if (meeting.getInsights().isEmpty()) {
                System.out.println("  Nenhum insight automático detectado.");
            } else {
                meeting.getInsights().forEach(System.out::println);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(DbExceptionMessages.format(e));
        }
    }

    public void atualizarSpiced() {
        long id = input.readLong("ID da oportunidade: ");
        System.out.println("Preencha os campos (Enter para manter em branco):");
        String situation = input.readLine("Situation: ");
        String pain = input.readLine("Pain: ");
        String impact = input.readLine("Impact: ");
        String criticalEvent = input.readLine("Critical Event: ");
        String decision = input.readLine("Decision: ");
        try {
            sistema.atualizarSpiced(id, situation, pain, impact, criticalEvent, decision);
            System.out.println("SPICED atualizado com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(DbExceptionMessages.format(e));
        }
    }

    public void gerarBriefing() {
        long id = input.readLong("ID da oportunidade: ");
        try {
            System.out.println(sistema.gerarBriefing(id));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(DbExceptionMessages.format(e));
        }
    }

    public void calcularHandoff() {
        long id = input.readLong("ID da oportunidade: ");
        try {
            System.out.println(sistema.calcularHandoffScore(id));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(DbExceptionMessages.format(e));
        }
    }

    public void exibirDashboardGeral() {
        try {
            System.out.println(sistema.gerarDashboard());
        } catch (RuntimeException e) {
            System.out.println(DbExceptionMessages.format(e));
        }
    }

    public void dashboardPorBu() {
        try {
            this.exibirBusinessUnits();
            BusinessUnit bu = BusinessUnit.fromInput(input.readRequiredLine("BU (1-3): "));
            System.out.println(sistema.gerarDashboardPorBu(bu));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(DbExceptionMessages.format(e));
        }
    }

    public void atualizarEtapa() {
        long id = input.readLong("ID da oportunidade: ");
        try {
            this.exibirPipelineStages();
            PipelineStage etapa = PipelineStage.fromInput(input.readRequiredLine("Nova etapa (1-7): "));
            if (sistema.atualizarEtapa(id, etapa)) {
                System.out.println("Etapa atualizada com sucesso.");
            } else {
                System.out.println("Oportunidade não encontrada.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(DbExceptionMessages.format(e));
        }
    }

    private void exibirBusinessUnits() {
        System.out.println("BU disponíveis:");
        System.out.println("  1 — TOTVS Gestão");
        System.out.println("  2 — RD Station");
        System.out.println("  3 — TOTVS Techfin");
    }

    private void exibirPipelineStages() {
        System.out.println("Etapas do pipeline:");
        System.out.println("  1 — Geração de demanda");
        System.out.println("  2 — Qualificação");
        System.out.println("  3 — Pré-vendas");
        System.out.println("  4 — Reunião diagnóstica");
        System.out.println("  5 — Proposta");
        System.out.println("  6 — Negociação");
        System.out.println("  7 — Fechamento");
    }
}
