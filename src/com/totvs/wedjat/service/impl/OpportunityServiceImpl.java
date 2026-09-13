package com.totvs.wedjat.service.impl;

import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.infrastructure.db.dao.OpportunityDAO;
import com.totvs.wedjat.service.IOpportunityService;

import java.util.List;
import java.util.Optional;

public class OpportunityServiceImpl implements IOpportunityService {

    private final OpportunityDAO dao = new OpportunityDAO();

    @Override
    public Opportunity cadastrar(
            String clientName,
            String product,
            BusinessUnit businessUnit,
            PipelineStage pipelineStage) {
        Opportunity opportunity = new Opportunity(clientName, product, businessUnit, pipelineStage);
        return dao.insert(opportunity);
    }

    @Override
    public List<Opportunity> listar() {
        return dao.findAll();
    }

    @Override
    public Optional<Opportunity> buscarPorId(long id) {
        return dao.findById(id);
    }

    @Override
    public boolean atualizarEtapa(long id, PipelineStage novaEtapa) {
        return dao.updateStage(id, novaEtapa);
    }
}
