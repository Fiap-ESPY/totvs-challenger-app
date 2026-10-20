package com.totvs.wedjat.api;

import com.totvs.wedjat.application.WedjatSystem;
import com.totvs.wedjat.domain.HandoffScore;
import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.infrastructure.db.DbExceptionMessages;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST do Wedjat. A fachada {@link WedjatSystem} continua dona das regras;
 * esta classe só traduz HTTP e JSON.
 */
public final class ApiServer {

    private static final int PORTA_PADRAO = 7070;

    private ApiServer() {
    }

    public static void main(String[] args) {
        int porta = PORTA_PADRAO;
        String portaEnv = System.getenv("PORT");
        if (portaEnv != null && !portaEnv.isBlank()) {
            porta = Integer.parseInt(portaEnv.trim());
        }
        Javalin app = criar(new WedjatSystem());
        app.start(porta);
        System.out.println("Wedjat API em http://localhost:" + porta);
    }

    public static Javalin criar(WedjatSystem sistema) {
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "web";
                staticFiles.location = Location.EXTERNAL;
            });
        });

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            String mensagem = e.getMessage() == null ? "Requisição inválida." : e.getMessage();
            int status = mensagem.contains("não encontrada") ? 404 : 400;
            responderErro(ctx, status, mensagem);
        });
        app.exception(Exception.class, (e, ctx) -> {
            if (e instanceof IllegalArgumentException) {
                return;
            }
            if (temSqlException(e)) {
                responderErro(ctx, 500, DbExceptionMessages.format(e));
                return;
            }
            responderErro(ctx, 500, "Falha interna ao processar a requisição.");
        });

        app.get("/oportunidades", ctx -> {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Opportunity opportunity : sistema.listarOportunidadesResumo()) {
                lista.add(JsonView.resumo(opportunity));
            }
            ctx.status(200).json(lista);
        });

        app.post("/oportunidades", ctx -> {
            Map<String, Object> body = corpo(ctx);
            Opportunity criada = sistema.cadastrarOportunidade(
                    texto(body, "cliente"),
                    texto(body, "produto"),
                    BusinessUnit.fromInput(texto(body, "bu")),
                    PipelineStage.fromInput(texto(body, "etapa")));
            ctx.status(201).json(JsonView.detalhe(criada));
        });

        app.get("/oportunidades/{id}", ctx -> {
            Opportunity opportunity = sistema.buscarOportunidade(id(ctx))
                    .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
            ctx.status(200).json(JsonView.detalhe(opportunity));
        });

        app.put("/oportunidades/{id}", ctx -> {
            Map<String, Object> body = corpo(ctx);
            Opportunity atualizada = sistema.atualizarOportunidade(
                    id(ctx),
                    texto(body, "cliente"),
                    texto(body, "produto"),
                    BusinessUnit.fromInput(texto(body, "bu")));
            ctx.status(200).json(JsonView.detalhe(atualizada));
        });

        app.delete("/oportunidades/{id}", ctx -> {
            sistema.excluirOportunidade(id(ctx));
            ctx.status(200).json(Map.of("mensagem", "Oportunidade excluída."));
        });

        app.patch("/oportunidades/{id}/etapa", ctx -> {
            Map<String, Object> body = corpo(ctx);
            long opportunityId = id(ctx);
            PipelineStage etapa = PipelineStage.fromInput(texto(body, "etapa"));
            if (!sistema.atualizarEtapa(opportunityId, etapa)) {
                throw new IllegalArgumentException("Oportunidade não encontrada.");
            }
            Opportunity opportunity = sistema.buscarOportunidade(opportunityId)
                    .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
            ctx.status(200).json(JsonView.detalhe(opportunity));
        });

        app.get("/oportunidades/{id}/reunioes", ctx -> {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (MeetingRecord meeting : sistema.listarReunioes(id(ctx))) {
                lista.add(JsonView.reuniao(meeting));
            }
            ctx.status(200).json(lista);
        });

        app.post("/oportunidades/{id}/reunioes", ctx -> {
            Map<String, Object> body = corpo(ctx);
            MeetingRecord meeting = sistema.registrarReuniao(id(ctx), texto(body, "transcricao"));
            ctx.status(201).json(JsonView.reuniao(meeting));
        });

        app.get("/oportunidades/{id}/spiced", ctx -> {
            Opportunity opportunity = sistema.buscarOportunidade(id(ctx))
                    .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
            ctx.status(200).json(JsonView.spiced(opportunity.getSpicedAssessment()));
        });

        app.put("/oportunidades/{id}/spiced", ctx -> {
            Map<String, Object> body = corpo(ctx);
            long opportunityId = id(ctx);
            sistema.atualizarSpiced(
                    opportunityId,
                    texto(body, "situation"),
                    texto(body, "pain"),
                    texto(body, "impact"),
                    texto(body, "criticalEvent"),
                    texto(body, "decision"));
            Opportunity opportunity = sistema.buscarOportunidade(opportunityId)
                    .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
            ctx.status(200).json(JsonView.spiced(opportunity.getSpicedAssessment()));
        });

        app.get("/oportunidades/{id}/briefing", ctx -> {
            long opportunityId = id(ctx);
            String briefing = sistema.gerarBriefing(opportunityId);
            Opportunity opportunity = sistema.buscarOportunidade(opportunityId)
                    .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("briefing", briefing);
            json.put("spiced", JsonView.spiced(opportunity.getSpicedAssessment()));
            ctx.status(200).json(json);
        });

        app.post("/oportunidades/{id}/handoff", ctx -> {
            HandoffScore handoff = sistema.calcularHandoff(id(ctx));
            ctx.status(200).json(JsonView.handoff(handoff));
        });

        app.get("/dashboard", ctx -> {
            List<Opportunity> oportunidades = sistema.listarOportunidadesResumo();
            ctx.status(200).json(JsonView.dashboard(
                    oportunidades, sistema.descreverDashboard(oportunidades)));
        });

        app.get("/dashboard/{bu}", ctx -> {
            BusinessUnit bu = BusinessUnit.fromInput(ctx.pathParam("bu"));
            List<Opportunity> todas = sistema.listarOportunidadesResumo();
            List<Opportunity> filtradas = new ArrayList<>();
            for (Opportunity opportunity : todas) {
                if (opportunity.getBusinessUnit() == bu) {
                    filtradas.add(opportunity);
                }
            }
            ctx.status(200).json(JsonView.dashboard(
                    filtradas, sistema.descreverDashboardPorBu(todas, bu)));
        });

        return app;
    }

    private static void responderErro(Context ctx, int status, String mensagem) {
        ctx.status(status).json(Map.of("erro", mensagem));
    }

    private static boolean temSqlException(Throwable erro) {
        Throwable atual = erro;
        while (atual != null) {
            if (atual instanceof SQLException) {
                return true;
            }
            atual = atual.getCause();
        }
        return false;
    }

    private static long id(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Id da oportunidade inválido.");
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> corpo(Context ctx) {
        String bruto = ctx.body();
        if (bruto == null || bruto.isBlank()) {
            throw new IllegalArgumentException("Corpo da requisição obrigatório.");
        }
        Object lido = ctx.bodyAsClass(Map.class);
        if (!(lido instanceof Map<?, ?> mapa)) {
            throw new IllegalArgumentException("Corpo da requisição deve ser um objeto JSON.");
        }
        return (Map<String, Object>) mapa;
    }

    private static String texto(Map<String, Object> body, String campo) {
        Object valor = body.get(campo);
        if (valor == null) {
            return null;
        }
        return String.valueOf(valor);
    }
}
