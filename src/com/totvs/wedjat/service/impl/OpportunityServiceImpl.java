package com.totvs.wedjat.service.impl;

import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.service.IOpportunityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OpportunityServiceImpl implements IOpportunityService {

    private final List<Opportunity> opportunities = new ArrayList<>();

    @Override
    public Opportunity cadastrar(
            String clientName,
            String product,
            BusinessUnit businessUnit,
            PipelineStage pipelineStage) {
        Opportunity opportunity =
                new Opportunity(clientName, product, businessUnit, pipelineStage);
        this.opportunities.add(opportunity);
        return opportunity;
    }

    @Override
    public List<Opportunity> listar() {
        return List.copyOf(this.opportunities);
    }

    @Override
    public Optional<Opportunity> buscarPorId(long id) {
        for (Opportunity o : this.opportunities) {
            if (o.getId() == id) {
                return Optional.of(o);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean atualizarEtapa(long id, PipelineStage novaEtapa) {
        Optional<Opportunity> encontrada = this.buscarPorId(id);
        if (encontrada.isEmpty()) {
            return false;
        }
        encontrada.get().setPipelineStage(novaEtapa);
        return true;
    }
}
