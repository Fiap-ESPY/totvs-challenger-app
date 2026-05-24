package com.totvs.wedjat.service;

import com.totvs.wedjat.domain.HandoffScore;
import com.totvs.wedjat.domain.Opportunity;

public interface IHandoffScoreService {

    int calcular(Opportunity opportunity);

    String classificar(int score);

    HandoffScore gerarHandoffScore(Opportunity opportunity);

    String gerarRelatorio(Opportunity opportunity);
}
