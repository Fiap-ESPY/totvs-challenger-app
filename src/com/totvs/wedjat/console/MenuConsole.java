package com.totvs.wedjat.console;

import com.totvs.wedjat.application.WedjatSystem;

import java.util.Scanner;

public final class MenuConsole {

    private final ConsoleInput input;
    private final ConsoleMenuHandler handler;

    public MenuConsole() {
        Scanner scanner = new Scanner(System.in);
        this.input = new ConsoleInput(scanner);
        this.handler = new ConsoleMenuHandler(input, new WedjatSystem());
    }

    public void executar() {
        System.out.println("Wedjat — Inteligência Comercial TOTVS");
        boolean continuar = true;
        while (continuar) {
            this.exibirOpcoes();
            String opcao = input.readLine("Escolha: ");
            continuar = this.processar(opcao);
        }
    }

    private void exibirOpcoes() {
        System.out.println();
        System.out.println("1 — Cadastrar oportunidade");
        System.out.println("2 — Listar oportunidades");
        System.out.println("3 — Registrar reunião/transcrição");
        System.out.println("4 — Atualizar campos SPICED");
        System.out.println("5 — Gerar briefing SPICED");
        System.out.println("6 — Calcular handoff score");
        System.out.println("7 — Ver dashboard geral");
        System.out.println("8 — Ver dashboard por BU");
        System.out.println("9 — Atualizar etapa do pipeline");
        System.out.println("0 — Sair");
    }

    private boolean processar(String opcao) {
        switch (opcao) {
            case "1":
                handler.cadastrarOportunidade();
                return true;
            case "2":
                handler.listarOportunidades();
                return true;
            case "3":
                handler.registrarReuniao();
                return true;
            case "4":
                handler.atualizarSpiced();
                return true;
            case "5":
                handler.gerarBriefing();
                return true;
            case "6":
                handler.calcularHandoff();
                return true;
            case "7":
                handler.exibirDashboardGeral();
                return true;
            case "8":
                handler.dashboardPorBu();
                return true;
            case "9":
                handler.atualizarEtapa();
                return true;
            case "0":
                System.out.println("Encerrando Wedjat.");
                return false;
            default:
                System.out.println("Opcao invalida.");
                return true;
        }
    }
}
