package com.totvs.wedjat.service;

import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;

import java.util.List;

public interface IDashboardService {

    String gerarResumo(List<Opportunity> opportunities);

    String gerarResumoPorBu(List<Opportunity> opportunities, BusinessUnit bu);
}
