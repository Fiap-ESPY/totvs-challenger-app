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
    public List<Opportunity> listarResumo() {
        return dao.findAllComSpiced();
    }

    @Override
    public Optional<Opportunity> buscarPorId(long id) {
        return dao.findById(id);
    }

    @Override
    public Opportunity atualizar(long id, String clientName, String product, BusinessUnit businessUnit) {
        Opportunity atual = dao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
        Opportunity validada = new Opportunity(
                atual.getId(),
                clientName,
                product,
                businessUnit,
                atual.getPipelineStage());
        if (!dao.update(id, validada.getClientName(), validada.getProduct(), validada.getBusinessUnit())) {
            throw new IllegalArgumentException("Oportunidade não encontrada.");
        }
        return dao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
    }

    @Override
    public boolean atualizarEtapa(long id, PipelineStage novaEtapa) {
        if (novaEtapa == null) {
            throw new IllegalArgumentException("Etapa do pipeline obrigatória.");
        }
        return dao.updateStage(id, novaEtapa);
    }

    @Override
    public void excluir(long id) {
        if (!dao.delete(id)) {
            throw new IllegalArgumentException("Oportunidade não encontrada.");
        }
    }
}
