package com.totvs.wedjat.service.impl;

import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.SpicedAssessment;
import com.totvs.wedjat.infrastructure.db.dao.SpicedAssessmentDAO;
import com.totvs.wedjat.service.ISpicedService;

import java.util.ArrayList;
import java.util.List;

public class SpicedServiceImpl implements ISpicedService {

    private final SpicedAssessmentDAO spicedDAO = new SpicedAssessmentDAO();

    @Override
    public void atualizarSpiced(
            Opportunity opportunity,
            String situation,
            String pain,
            String impact,
            String criticalEvent,
            String decision) {
        SpicedAssessment spiced = opportunity.getSpicedAssessment();
        if (situation != null && !situation.isBlank())     spiced.setSituation(situation);
        if (pain != null && !pain.isBlank())               spiced.setPain(pain);
        if (impact != null && !impact.isBlank())           spiced.setImpact(impact);
        if (criticalEvent != null && !criticalEvent.isBlank()) spiced.setCriticalEvent(criticalEvent);
        if (decision != null && !decision.isBlank())       spiced.setDecision(decision);

        spicedDAO.update(spiced);
    }

    @Override
    public String gerarBriefing(Opportunity opportunity) {
        SpicedAssessment spiced = opportunity.getSpicedAssessment();
        List<String> lacunas  = identificarLacunas(spiced);
        List<String> perguntas = sugerirPerguntas(lacunas);

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
        briefing.append("--- Situation ---\n").append(formatarCampo(spiced.getSituation())).append('\n');
        briefing.append("--- Pain ---\n").append(formatarCampo(spiced.getPain())).append('\n');
        briefing.append("--- Impact ---\n").append(formatarCampo(spiced.getImpact())).append('\n');
        briefing.append("--- Critical Event ---\n").append(formatarCampo(spiced.getCriticalEvent())).append('\n');
        briefing.append("--- Decision ---\n").append(formatarCampo(spiced.getDecision())).append('\n');
        briefing.append('\n');

        if (spiced.hasImpactGap()) {
            briefing.append("ALERTA: Dor identificada sem impacto de negócio quantificado.\n");
        }

        briefing.append("Lacunas: ");
        briefing.append(lacunas.isEmpty() ? "nenhuma\n" : String.join(", ", lacunas) + '\n');

        briefing.append("\nPerguntas sugeridas:\n");
        perguntas.forEach(p -> briefing.append("  - ").append(p).append('\n'));

        return briefing.toString();
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private List<String> identificarLacunas(SpicedAssessment spiced) {
        List<String> lacunas = new ArrayList<>();
        if (spiced.getSituation()    == null) lacunas.add("Situation");
        if (spiced.getPain()         == null) lacunas.add("Pain");
        if (spiced.getImpact()       == null) lacunas.add("Impact");
        if (spiced.getCriticalEvent() == null) lacunas.add("Critical Event");
        if (spiced.getDecision()     == null) lacunas.add("Decision");
        return lacunas;
    }

    private List<String> sugerirPerguntas(List<String> lacunas) {
        List<String> perguntas = new ArrayList<>();
        for (String lacuna : lacunas) {
            switch (lacuna) {
                case "Situation"     -> perguntas.add("Qual e o contexto atual do cliente e quais sistemas ja utiliza?");
                case "Pain"          -> perguntas.add("Qual dor operacional ou estrategica esta motivando esta conversa?");
                case "Impact"        -> perguntas.add("Qual impacto financeiro ou de negocio essa dor gera hoje?");
                case "Critical Event"-> perguntas.add("Existe um evento critico que exige decisao em um prazo definido?");
                case "Decision"      -> perguntas.add("Quem sao os decisores e qual o processo de aprovacao?");
                default -> {}
            }
        }
        if (perguntas.isEmpty()) {
            perguntas.add("Validar proximo passo comercial e data de follow-up.");
        }
        return perguntas;
    }

    private String formatarCampo(String valor) {
        return valor == null ? "[não preenchido]" : valor;
    }
}
