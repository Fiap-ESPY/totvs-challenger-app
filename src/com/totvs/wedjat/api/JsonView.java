package com.totvs.wedjat.api;

import com.totvs.wedjat.domain.HandoffScore;
import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.SpicedAssessment;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.domain.insight.Insight;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converte o domínio em JSON estável, sem serializar o grafo de objetos
 * (HandoffScore aponta de volta para Opportunity).
 */
public final class JsonView {

    private JsonView() {
    }

    public static Map<String, Object> resumo(Opportunity opportunity) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", opportunity.getId());
        json.put("cliente", opportunity.getClientName());
        json.put("produto", opportunity.getProduct());
        json.put("bu", opportunity.getBusinessUnit().name());
        json.put("buDescricao", opportunity.getBusinessUnit().getDescricao());
        json.put("etapa", opportunity.getPipelineStage().name());
        json.put("etapaDescricao", opportunity.getPipelineStage().getDescricao());
        json.put("spicedPreenchidos", opportunity.getSpicedAssessment().countPreenchidos());
        json.put("reunioes", opportunity.getMeetings().size());
        json.put("risco", opportunity.hasRisk());
        json.put("impactGap", opportunity.getSpicedAssessment().hasImpactGap());
        return json;
    }

    public static Map<String, Object> detalhe(Opportunity opportunity) {
        Map<String, Object> json = resumo(opportunity);
        json.put("spiced", spiced(opportunity.getSpicedAssessment()));
        List<Map<String, Object>> reunioes = new ArrayList<>();
        for (MeetingRecord meeting : opportunity.getMeetings()) {
            reunioes.add(reuniao(meeting));
        }
        json.put("reunioesDetalhe", reunioes);
        return json;
    }

    public static Map<String, Object> spiced(SpicedAssessment spiced) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", spiced.getId());
        json.put("situation", spiced.getSituation());
        json.put("pain", spiced.getPain());
        json.put("impact", spiced.getImpact());
        json.put("criticalEvent", spiced.getCriticalEvent());
        json.put("decision", spiced.getDecision());
        json.put("preenchidos", spiced.countPreenchidos());
        json.put("impactGap", spiced.hasImpactGap());
        return json;
    }

    public static Map<String, Object> reuniao(MeetingRecord meeting) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", meeting.getId());
        json.put("data", meeting.getDate().toString());
        json.put("transcricao", meeting.getTranscription());
        List<Map<String, Object>> insights = new ArrayList<>();
        for (Insight insight : meeting.getInsights()) {
            insights.add(insight(insight));
        }
        json.put("insights", insights);
        return json;
    }

    public static Map<String, Object> insight(Insight insight) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", insight.getId());
        json.put("tipo", insight.getType().name());
        json.put("tipoDescricao", insight.getType().getDescricao());
        json.put("prioridade", insight.getPrioridade());
        json.put("descricao", insight.getDescription());
        json.put("evidencia", insight.getEvidence());
        return json;
    }

    public static Map<String, Object> handoff(HandoffScore handoff) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", handoff.getId());
        json.put("score", handoff.getScore());
        json.put("classificacao", handoff.getClassificacao());
        json.put("camposSpicedPreenchidos", handoff.getCamposSpicedPreenchidos());
        json.put("relatorio", handoff.toString());
        return json;
    }

    public static Map<String, Object> dashboard(List<Opportunity> oportunidades, String texto) {
        Map<String, Object> json = new LinkedHashMap<>();
        int comRisco = 0;
        int comImpactGap = 0;
        for (Opportunity opportunity : oportunidades) {
            if (opportunity.hasRisk()) {
                comRisco++;
            }
            if (opportunity.getSpicedAssessment().hasImpactGap()) {
                comImpactGap++;
            }
        }
        json.put("total", oportunidades.size());
        json.put("comRisco", comRisco);
        json.put("comImpactGap", comImpactGap);
        json.put("porBu", contarBu(oportunidades));
        json.put("porEtapa", contarEtapa(oportunidades));
        json.put("texto", texto);
        return json;
    }

    private static List<Map<String, Object>> contarBu(List<Opportunity> oportunidades) {
        List<Map<String, Object>> grupos = new ArrayList<>();
        for (BusinessUnit bu : BusinessUnit.values()) {
            int quantidade = 0;
            for (Opportunity opportunity : oportunidades) {
                if (opportunity.getBusinessUnit() == bu) {
                    quantidade++;
                }
            }
            Map<String, Object> grupo = new LinkedHashMap<>();
            grupo.put("bu", bu.name());
            grupo.put("descricao", bu.getDescricao());
            grupo.put("quantidade", quantidade);
            grupos.add(grupo);
        }
        return grupos;
    }

    private static List<Map<String, Object>> contarEtapa(List<Opportunity> oportunidades) {
        List<Map<String, Object>> grupos = new ArrayList<>();
        for (PipelineStage etapa : PipelineStage.values()) {
            int quantidade = 0;
            for (Opportunity opportunity : oportunidades) {
                if (opportunity.getPipelineStage() == etapa) {
                    quantidade++;
                }
            }
            Map<String, Object> grupo = new LinkedHashMap<>();
            grupo.put("etapa", etapa.name());
            grupo.put("descricao", etapa.getDescricao());
            grupo.put("quantidade", quantidade);
            grupos.add(grupo);
        }
        return grupos;
    }
}
