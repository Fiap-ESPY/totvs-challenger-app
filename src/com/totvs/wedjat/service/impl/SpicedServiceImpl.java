package com.totvs.wedjat.service.impl;

import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.SpicedAssessment;
import com.totvs.wedjat.service.ISpicedService;

import java.util.ArrayList;
import java.util.List;

public class SpicedServiceImpl implements ISpicedService {

    @Override
    public void atualizarSpiced(
            Opportunity opportunity,
            String situation,
            String pain,
            String impact,
            String criticalEvent,
            String decision) {
        SpicedAssessment spiced = opportunity.getSpicedAssessment();
        if (situation != null && !situation.isBlank()) {
            spiced.setSituation(situation);
        }
        if (pain != null && !pain.isBlank()) {
            spiced.setPain(pain);
        }
        if (impact != null && !impact.isBlank()) {
            spiced.setImpact(impact);
        }
        if (criticalEvent != null && !criticalEvent.isBlank()) {
            spiced.setCriticalEvent(criticalEvent);
        }
        if (decision != null && !decision.isBlank()) {
            spiced.setDecision(decision);
        }
    }

    @Override
    public String gerarBriefing(Opportunity opportunity) {
        SpicedAssessment spiced = opportunity.getSpicedAssessment();
        List<String> lacunas = this.identificarLacunas(spiced);
        List<String> perguntas = this.sugerirPerguntas(lacunas);

        StringBuilder briefing = new StringBuilder();
        briefing.append("================================\n");
        briefing.append("   BRIEFING SPICED\n");
        briefing.append("================================\n");
        briefing.append("SPICED ID: ").append(spiced.getId()).append('\n');
        briefing.append("Cliente: ").append(opportunity.getClientName()).append('\n');
        briefing.append("Produto: ").append(opportunity.getProduct()).append('\n');
        briefing.append("BU: ").append(opportunity.getBusinessUnit().getDescricao()).append('\n');
        briefing.append("Etapa: ").append(opportunity.getPipelineStage().getDescricao()).append('\n');
        briefing.append('\n');
        briefing.append("--- Situation ---\n");
        briefing.append(this.formatarCampo(spiced.getSituation())).append('\n');
        briefing.append("--- Pain ---\n");
        briefing.append(this.formatarCampo(spiced.getPain())).append('\n');
        briefing.append("--- Impact ---\n");
        briefing.append(this.formatarCampo(spiced.getImpact())).append('\n');
        briefing.append("--- Critical Event ---\n");
        briefing.append(this.formatarCampo(spiced.getCriticalEvent())).append('\n');
        briefing.append("--- Decision ---\n");
        briefing.append(this.formatarCampo(spiced.getDecision())).append('\n');
        briefing.append('\n');

        if (spiced.hasImpactGap()) {
            briefing.append("ALERTA: Dor identificada sem impacto de negócio quantificado.\n");
        }

        briefing.append("Lacunas: ");
        if (lacunas.isEmpty()) {
            briefing.append("nenhuma\n");
        } else {
            briefing.append(String.join(", ", lacunas)).append('\n');
        }

        briefing.append("\nPerguntas sugeridas:\n");
        for (String pergunta : perguntas) {
            briefing.append("  - ").append(pergunta).append('\n');
        }

        return briefing.toString();
    }

    private List<String> identificarLacunas(SpicedAssessment spiced) {
        List<String> lacunas = new ArrayList<>();
        if (spiced.getSituation() == null) lacunas.add("Situation");
        if (spiced.getPain() == null) lacunas.add("Pain");
        if (spiced.getImpact() == null) lacunas.add("Impact");
        if (spiced.getCriticalEvent() == null) lacunas.add("Critical Event");
        if (spiced.getDecision() == null) lacunas.add("Decision");
        return lacunas;
    }

    private List<String> sugerirPerguntas(List<String> lacunas) {
        List<String> perguntas = new ArrayList<>();
        for (String lacuna : lacunas) {
            switch (lacuna) {
                case "Situation":
                    perguntas.add("Qual e o contexto atual do cliente e quais sistemas ja utiliza?");
                    break;
                case "Pain":
                    perguntas.add("Qual dor operacional ou estrategica esta motivando esta conversa?");
                    break;
                case "Impact":
                    perguntas.add("Qual impacto financeiro ou de negocio essa dor gera hoje?");
                    break;
                case "Critical Event":
                    perguntas.add("Existe um evento critico que exige decisao em um prazo definido?");
                    break;
                case "Decision":
                    perguntas.add("Quem sao os decisores e qual o processo de aprovacao?");
                    break;
                default:
                    break;
            }
        }
        if (perguntas.isEmpty()) {
            perguntas.add("Validar próximo passo comercial e data de follow-up.");
        }
        return perguntas;
    }

    private String formatarCampo(String valor) {
        return valor == null ? "[não preenchido]" : valor;
    }
}
