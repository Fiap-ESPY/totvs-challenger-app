package com.totvs.wedjat.service.impl;

import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.service.IDashboardService;

import java.util.ArrayList;
import java.util.List;

public class DashboardServiceImpl implements IDashboardService {

    @Override
    public String gerarResumo(List<Opportunity> opportunities) {
        if (opportunities.isEmpty()) {
            return "Nenhuma oportunidade cadastrada para exibir no dashboard.";
        }

        int comRisco = 0;
        int comImpactGap = 0;

        for (Opportunity opportunity : opportunities) {
            if (opportunity.hasRisk()) {
                comRisco++;
            }
            if (opportunity.getSpicedAssessment().hasImpactGap()) {
                comImpactGap++;
            }
        }

        StringBuilder dashboard = new StringBuilder();
        dashboard.append("================================\n");
        dashboard.append("   DASHBOARD WEDJAT\n");
        dashboard.append("================================\n");
        dashboard.append("Total de oportunidades: ").append(opportunities.size()).append('\n');
        dashboard.append("Com sinal de risco:     ").append(comRisco).append('\n');
        dashboard.append("Com gap de Impact:      ").append(comImpactGap).append('\n');
        dashboard.append('\n');
        dashboard.append("--- Por BU ---\n");
        for (BusinessUnit bu : BusinessUnit.values()) {
            int quantidade = this.contarPorBu(opportunities, bu);
            if (quantidade > 0) {
                dashboard.append("  ")
                        .append(bu.getDescricao())
                        .append(": ")
                        .append(quantidade)
                        .append('\n');
            }
        }
        dashboard.append('\n');
        dashboard.append("--- Por etapa do pipeline ---\n");
        for (PipelineStage etapa : PipelineStage.values()) {
            int quantidade = this.contarPorEtapa(opportunities, etapa);
            if (quantidade > 0) {
                dashboard.append("  ")
                        .append(etapa.getDescricao())
                        .append(": ")
                        .append(quantidade)
                        .append('\n');
            }
        }

        return dashboard.toString();
    }

    @Override
    public String gerarResumoPorBu(List<Opportunity> opportunities, BusinessUnit bu) {
        List<Opportunity> filtradas = new ArrayList<>();

        for (Opportunity o : opportunities) {
            if (bu == o.getBusinessUnit()) {
                filtradas.add(o);
            }
        }

        if (filtradas.isEmpty()) {
            return "Nenhuma oportunidade encontrada para a BU: " + bu.getDescricao();
        }

        StringBuilder resumo = new StringBuilder();
        resumo.append("=== Dashboard — ").append(bu.getDescricao()).append(" ===\n");
        resumo.append("Oportunidades: ").append(filtradas.size()).append('\n');

        int comRisco = 0;
        for (Opportunity o : filtradas) {
            if (o.hasRisk()) {
                comRisco++;
            }
        }
        resumo.append("Com risco: ").append(comRisco).append('\n');

        for (Opportunity o : filtradas) {
            resumo.append("  #").append(o.getId()).append(" — ").append(o.getClientName());
            resumo.append(" [").append(o.getPipelineStage().getDescricao()).append("]\n");
        }

        return resumo.toString();
    }

    private int contarPorBu(List<Opportunity> opportunities, BusinessUnit bu) {
        int quantidade = 0;
        for (Opportunity o : opportunities) {
            if (bu == o.getBusinessUnit()) {
                quantidade++;
            }
        }
        return quantidade;
    }

    private int contarPorEtapa(List<Opportunity> opportunities, PipelineStage etapa) {
        int quantidade = 0;
        for (Opportunity o : opportunities) {
            if (etapa == o.getPipelineStage()) {
                quantidade++;
            }
        }
        return quantidade;
    }
}
