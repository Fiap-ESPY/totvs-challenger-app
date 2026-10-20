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

    /** Resumo para API/dashboard (oportunidade + SPICED, sem reuniões). */
    List<Opportunity> listarResumo();

    Optional<Opportunity> buscarPorId(long id);

    Opportunity atualizar(long id, String clientName, String product, BusinessUnit businessUnit);

    boolean atualizarEtapa(long id, PipelineStage novaEtapa);

    void excluir(long id);
}
