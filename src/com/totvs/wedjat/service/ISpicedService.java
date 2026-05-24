package com.totvs.wedjat.service;

import com.totvs.wedjat.domain.Opportunity;

public interface ISpicedService {

    void atualizarSpiced(
            Opportunity opportunity,
            String situation,
            String pain,
            String impact,
            String criticalEvent,
            String decision);

    String gerarBriefing(Opportunity opportunity);
}
