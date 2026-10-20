package com.totvs.wedjat.api.web;

import com.totvs.wedjat.api.JsonView;
import com.totvs.wedjat.application.WedjatSystem;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class DashboardRestController {

    private final WedjatSystem sistema;

    public DashboardRestController(WedjatSystem sistema) {
        this.sistema = sistema;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> geral() {
        List<Opportunity> oportunidades = sistema.listarOportunidadesResumo();
        return JsonView.dashboard(oportunidades, sistema.descreverDashboard(oportunidades));
    }

    @GetMapping("/dashboard/{bu}")
    public Map<String, Object> porBu(@PathVariable String bu) {
        BusinessUnit businessUnit = BusinessUnit.fromInput(bu);
        List<Opportunity> todas = sistema.listarOportunidadesResumo();
        List<Opportunity> filtradas = new ArrayList<>();
        for (Opportunity opportunity : todas) {
            if (opportunity.getBusinessUnit() == businessUnit) {
                filtradas.add(opportunity);
            }
        }
        return JsonView.dashboard(filtradas, sistema.descreverDashboardPorBu(todas, businessUnit));
    }
}
