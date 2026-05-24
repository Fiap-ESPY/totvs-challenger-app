package com.totvs.wedjat.application;

import com.totvs.wedjat.domain.HandoffScore;
import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.service.IDashboardService;
import com.totvs.wedjat.service.IHandoffScoreService;
import com.totvs.wedjat.service.IOpportunityService;
import com.totvs.wedjat.service.ISpicedService;
import com.totvs.wedjat.service.ITranscriptionService;
import com.totvs.wedjat.service.impl.DashboardServiceImpl;
import com.totvs.wedjat.service.impl.HandoffScoreServiceImpl;
import com.totvs.wedjat.service.impl.OpportunityServiceImpl;
import com.totvs.wedjat.service.impl.SpicedServiceImpl;
import com.totvs.wedjat.service.impl.TranscriptionServiceImpl;

import java.util.List;
import java.util.Optional;

public class WedjatSystem {

    private final IOpportunityService opportunityService = new OpportunityServiceImpl();
    private final ISpicedService spicedService = new SpicedServiceImpl();
    private final ITranscriptionService transcriptionService = new TranscriptionServiceImpl();
    private final IHandoffScoreService handoffScoreService = new HandoffScoreServiceImpl();
    private final IDashboardService dashboardService = new DashboardServiceImpl();

    public Opportunity cadastrarOportunidade(
            String clientName,
            String product,
            BusinessUnit businessUnit,
            PipelineStage pipelineStage) {
        return this.opportunityService.cadastrar(clientName, product, businessUnit, pipelineStage);
    }

    public List<Opportunity> listarOportunidades() {
        return this.opportunityService.listar();
    }

    public Optional<Opportunity> buscarOportunidade(long id) {
        return this.opportunityService.buscarPorId(id);
    }

    public boolean atualizarEtapa(long id, PipelineStage novaEtapa) {
        return this.opportunityService.atualizarEtapa(id, novaEtapa);
    }

    public void atualizarSpiced(
            long opportunityId,
            String situation,
            String pain,
            String impact,
            String criticalEvent,
            String decision) {
        Optional<Opportunity> opportunity = this.buscarOportunidade(opportunityId);
        if (!opportunity.isPresent()) {
            throw new IllegalArgumentException("Oportunidade não encontrada.");
        }
        this.spicedService.atualizarSpiced(
                opportunity.get(), situation, pain, impact, criticalEvent, decision);
    }

    public String gerarBriefing(long opportunityId) {
        Opportunity opportunity = this.buscarOportunidade(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
        return this.spicedService.gerarBriefing(opportunity);
    }

    public MeetingRecord registrarReuniao(long opportunityId, String transcription) {
        Opportunity opportunity = this.buscarOportunidade(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
        return this.transcriptionService.registrarReuniao(opportunity, transcription);
    }

    public String calcularHandoffScore(long opportunityId) {
        Opportunity opportunity = this.buscarOportunidade(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
        HandoffScore handoffScore = this.handoffScoreService.gerarHandoffScore(opportunity);
        opportunity.setHandoffScore(handoffScore);
        return handoffScore.toString();
    }

    public String gerarDashboard() {
        return this.dashboardService.gerarResumo(this.opportunityService.listar());
    }

    public String gerarDashboardPorBu(BusinessUnit businessUnit) {
        return this.dashboardService.gerarResumoPorBu(this.opportunityService.listar(), businessUnit);
    }
}
