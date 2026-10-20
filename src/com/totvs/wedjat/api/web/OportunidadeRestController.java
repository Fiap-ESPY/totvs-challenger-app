package com.totvs.wedjat.api.web;

import com.totvs.wedjat.api.JsonView;
import com.totvs.wedjat.application.WedjatSystem;
import com.totvs.wedjat.domain.HandoffScore;
import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class OportunidadeRestController {

    private final WedjatSystem sistema;

    public OportunidadeRestController(WedjatSystem sistema) {
        this.sistema = sistema;
    }

    @GetMapping("/oportunidades")
    public List<Map<String, Object>> listar() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Opportunity opportunity : sistema.listarOportunidadesResumo()) {
            lista.add(JsonView.resumo(opportunity));
        }
        return lista;
    }

    @PostMapping("/oportunidades")
    public ResponseEntity<Map<String, Object>> criar(@RequestBody Map<String, Object> body) {
        Opportunity criada = sistema.cadastrarOportunidade(
                ApiRequestHelper.texto(body, "cliente"),
                ApiRequestHelper.texto(body, "produto"),
                BusinessUnit.fromInput(ApiRequestHelper.texto(body, "bu")),
                PipelineStage.fromInput(ApiRequestHelper.texto(body, "etapa")));
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonView.detalhe(criada));
    }

    @GetMapping("/oportunidades/{id}")
    public Map<String, Object> buscar(@PathVariable String id) {
        Opportunity opportunity = sistema.buscarOportunidade(ApiRequestHelper.parseId(id))
                .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
        return JsonView.detalhe(opportunity);
    }

    @PutMapping("/oportunidades/{id}")
    public Map<String, Object> atualizar(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Opportunity atualizada = sistema.atualizarOportunidade(
                ApiRequestHelper.parseId(id),
                ApiRequestHelper.texto(body, "cliente"),
                ApiRequestHelper.texto(body, "produto"),
                BusinessUnit.fromInput(ApiRequestHelper.texto(body, "bu")));
        return JsonView.detalhe(atualizada);
    }

    @DeleteMapping("/oportunidades/{id}")
    public Map<String, String> excluir(@PathVariable String id) {
        sistema.excluirOportunidade(ApiRequestHelper.parseId(id));
        return Map.of("mensagem", "Oportunidade excluída.");
    }

    @PatchMapping("/oportunidades/{id}/etapa")
    public Map<String, Object> atualizarEtapa(@PathVariable String id, @RequestBody Map<String, Object> body) {
        long opportunityId = ApiRequestHelper.parseId(id);
        PipelineStage etapa = PipelineStage.fromInput(ApiRequestHelper.texto(body, "etapa"));
        if (!sistema.atualizarEtapa(opportunityId, etapa)) {
            throw new IllegalArgumentException("Oportunidade não encontrada.");
        }
        Opportunity opportunity = sistema.buscarOportunidade(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
        return JsonView.detalhe(opportunity);
    }

    @GetMapping("/oportunidades/{id}/reunioes")
    public List<Map<String, Object>> listarReunioes(@PathVariable String id) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (MeetingRecord meeting : sistema.listarReunioes(ApiRequestHelper.parseId(id))) {
            lista.add(JsonView.reuniao(meeting));
        }
        return lista;
    }

    @PostMapping("/oportunidades/{id}/reunioes")
    public ResponseEntity<Map<String, Object>> registrarReuniao(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        MeetingRecord meeting = sistema.registrarReuniao(
                ApiRequestHelper.parseId(id),
                ApiRequestHelper.texto(body, "transcricao"));
        return ResponseEntity.status(HttpStatus.CREATED).body(JsonView.reuniao(meeting));
    }

    @GetMapping("/oportunidades/{id}/spiced")
    public Map<String, Object> obterSpiced(@PathVariable String id) {
        Opportunity opportunity = sistema.buscarOportunidade(ApiRequestHelper.parseId(id))
                .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
        return JsonView.spiced(opportunity.getSpicedAssessment());
    }

    @PutMapping("/oportunidades/{id}/spiced")
    public Map<String, Object> atualizarSpiced(@PathVariable String id, @RequestBody Map<String, Object> body) {
        long opportunityId = ApiRequestHelper.parseId(id);
        sistema.atualizarSpiced(
                opportunityId,
                ApiRequestHelper.texto(body, "situation"),
                ApiRequestHelper.texto(body, "pain"),
                ApiRequestHelper.texto(body, "impact"),
                ApiRequestHelper.texto(body, "criticalEvent"),
                ApiRequestHelper.texto(body, "decision"));
        Opportunity opportunity = sistema.buscarOportunidade(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
        return JsonView.spiced(opportunity.getSpicedAssessment());
    }

    @GetMapping("/oportunidades/{id}/briefing")
    public Map<String, Object> briefing(@PathVariable String id) {
        long opportunityId = ApiRequestHelper.parseId(id);
        String briefing = sistema.gerarBriefing(opportunityId);
        Opportunity opportunity = sistema.buscarOportunidade(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Oportunidade não encontrada."));
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("briefing", briefing);
        json.put("spiced", JsonView.spiced(opportunity.getSpicedAssessment()));
        return json;
    }

    @PostMapping("/oportunidades/{id}/handoff")
    public Map<String, Object> handoff(@PathVariable String id) {
        HandoffScore score = sistema.calcularHandoff(ApiRequestHelper.parseId(id));
        return JsonView.handoff(score);
    }
}
