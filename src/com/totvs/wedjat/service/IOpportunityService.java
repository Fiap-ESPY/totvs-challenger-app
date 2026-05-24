package com.totvs.wedjat.service;

import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;

import java.util.List;
import java.util.Optional;

public interface IOpportunityService {

    Opportunity cadastrar(
            String clientName,
            String product,
            BusinessUnit businessUnit,
            PipelineStage pipelineStage);

    List<Opportunity> listar();

    Optional<Opportunity> buscarPorId(long id);

    boolean atualizarEtapa(long id, PipelineStage novaEtapa);
}
