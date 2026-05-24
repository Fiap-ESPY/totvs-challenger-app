package com.totvs.wedjat.service;

import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.insight.Insight;

import java.util.List;

public interface ITranscriptionService {

    MeetingRecord registrarReuniao(Opportunity opportunity, String transcription);

    List<Insight> analisarTranscricao(String transcription);
}
