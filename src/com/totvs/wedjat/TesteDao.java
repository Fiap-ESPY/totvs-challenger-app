package com.totvs.wedjat;

import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.infrastructure.db.ConnectionFactory;
import com.totvs.wedjat.service.impl.HandoffScoreServiceImpl;
import com.totvs.wedjat.service.impl.OpportunityServiceImpl;
import com.totvs.wedjat.service.impl.SpicedServiceImpl;
import com.totvs.wedjat.service.impl.TranscriptionServiceImpl;

import java.sql.Connection;
import java.util.List;

/**
 * Classe de teste para validar o CRUD completo via camada DAO.
 * Execute com: java -cp "out:lib/ojdbc11.jar" com.totvs.wedjat.TesteDao
 */
public class TesteDao {

    private static final OpportunityServiceImpl  opportunityService  = new OpportunityServiceImpl();
    private static final SpicedServiceImpl       spicedService       = new SpicedServiceImpl();
    private static final TranscriptionServiceImpl transcriptionService = new TranscriptionServiceImpl();
    private static final HandoffScoreServiceImpl handoffService      = new HandoffScoreServiceImpl();

    public static void main(String[] args) {
        titulo("WEDJAT — TESTE DAO");

        testarConexao();
        Opportunity opp  = testarInsert();
        testarFindAll();
        testarFindById(opp.getId());
        testarUpdateStage(opp.getId());
        testarSpiced(opp);
        testarReuniao(opp);
        testarHandoff(opp);
        testarDelete(opp.getId());
        testarFindAll(); // confirma remoção

        titulo("TESTE CONCLUÍDO");
    }

    // -------------------------------------------------------------------------
    // 1. Conexão
    // -------------------------------------------------------------------------
    private static void testarConexao() {
        secao("1. Conexão com o banco");
        try (Connection conn = ConnectionFactory.getConnection()) {
            System.out.println("✅ Conectado: " + conn.getMetaData().getDatabaseProductName()
                    + " " + conn.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            falha("Erro de conexão", e);
        }
    }

    // -------------------------------------------------------------------------
    // 2. INSERT — cadastrar oportunidade
    // -------------------------------------------------------------------------
    private static Opportunity testarInsert() {
        secao("2. INSERT — cadastrar oportunidade");
        Opportunity opp = opportunityService.cadastrar(
                "Empresa Teste LTDA",
                "Protheus",
                BusinessUnit.TOTVS_GESTAO,
                PipelineStage.QUALIFICACAO);

        System.out.println("✅ Oportunidade inserida com ID: " + opp.getId());
        System.out.println("   SPICED ID: " + opp.getSpicedAssessment().getId());
        return opp;
    }

    // -------------------------------------------------------------------------
    // 3. SELECT — listar todas
    // -------------------------------------------------------------------------
    private static void testarFindAll() {
        secao("3. SELECT — listar oportunidades");
        List<Opportunity> lista = opportunityService.listar();
        System.out.println("✅ Total encontrado: " + lista.size());
        lista.forEach(o -> System.out.println("   #" + o.getId() + " — " + o.getClientName()
                + " [" + o.getPipelineStage().getDescricao() + "]"));
    }

    // -------------------------------------------------------------------------
    // 4. SELECT por PK
    // -------------------------------------------------------------------------
    private static void testarFindById(long id) {
        secao("4. SELECT por PK — buscar id=" + id);
        opportunityService.buscarPorId(id).ifPresentOrElse(
                o -> System.out.println("✅ Encontrado: " + o.getClientName()
                        + " | SPICED campos: " + o.getSpicedAssessment().countPreenchidos()),
                () -> System.out.println("❌ Oportunidade não encontrada"));
    }

    // -------------------------------------------------------------------------
    // 5. UPDATE — atualizar etapa
    // -------------------------------------------------------------------------
    private static void testarUpdateStage(long id) {
        secao("5. UPDATE — atualizar etapa para PRE_VENDAS");
        boolean ok = opportunityService.atualizarEtapa(id, PipelineStage.PRE_VENDAS);
        System.out.println(ok ? "✅ Etapa atualizada" : "❌ Oportunidade não encontrada");

        opportunityService.buscarPorId(id).ifPresent(
                o -> System.out.println("   Nova etapa confirmada: " + o.getPipelineStage().getDescricao()));
    }

    // -------------------------------------------------------------------------
    // 6. UPDATE — atualizar SPICED manualmente
    // -------------------------------------------------------------------------
    private static void testarSpiced(Opportunity opp) {
        secao("6. UPDATE — preencher campos SPICED");
        spicedService.atualizarSpiced(opp,
                "Cliente usa ERP legado com processos manuais",
                "Retrabalho elevado no fechamento contábil",
                "Perda de 40h/mês de produtividade",
                "Auditoria fiscal em dezembro",
                "CFO e Diretora de TI");

        System.out.println("✅ SPICED atualizado. Campos preenchidos: "
                + opp.getSpicedAssessment().countPreenchidos() + "/5");
    }

    // -------------------------------------------------------------------------
    // 7. INSERT — registrar reunião + insights
    // -------------------------------------------------------------------------
    private static void testarReuniao(Opportunity opp) {
        secao("7. INSERT — registrar reunião com transcrição");
        String transcricao =
                "Cliente usa Protheus há 5 anos, tem dor em integração fiscal. " +
                "CFO bloqueou orçamento, comparou com SAP. Deadline em dezembro.";

        MeetingRecord meeting = transcriptionService.registrarReuniao(opp, transcricao);

        System.out.println("✅ Reunião inserida com ID: " + meeting.getId());
        System.out.println("   Insights gerados: " + meeting.getInsights().size());
        meeting.getInsights().forEach(
                i -> System.out.println("   → [" + i.getType().getDescricao() + "] " + i.getDescription()));
    }

    // -------------------------------------------------------------------------
    // 8. INSERT/UPDATE — calcular handoff score
    // -------------------------------------------------------------------------
    private static void testarHandoff(Opportunity opp) {
        secao("8. INSERT — calcular e persistir handoff score");
        String relatorio = handoffService.gerarRelatorio(opp);
        System.out.println("✅ Handoff calculado e persistido:");
        System.out.println(relatorio);
    }

    // -------------------------------------------------------------------------
    // 9. DELETE — remover oportunidade (cascade: SPICED, meetings, insights, handoff)
    // -------------------------------------------------------------------------
    private static void testarDelete(long id) {
        secao("9. DELETE — remover oportunidade id=" + id);
        boolean ok = new com.totvs.wedjat.infrastructure.db.dao.OpportunityDAO().delete(id);
        System.out.println(ok ? "✅ Oportunidade e registros filhos removidos (cascade)"
                : "❌ Oportunidade não encontrada");
    }

    // -------------------------------------------------------------------------
    // Formatação
    // -------------------------------------------------------------------------
    private static void titulo(String texto) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  " + texto);
        System.out.println("=".repeat(50));
    }

    private static void secao(String texto) {
        System.out.println("\n--- " + texto + " ---");
    }

    private static void falha(String msg, Exception e) {
        System.out.println("❌ " + msg + ": " + e.getMessage());
        System.exit(1);
    }
}
