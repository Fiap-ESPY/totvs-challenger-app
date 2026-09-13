package com.totvs.wedjat;

import com.totvs.wedjat.domain.MeetingRecord;
import com.totvs.wedjat.domain.Opportunity;
import com.totvs.wedjat.domain.enums.BusinessUnit;
import com.totvs.wedjat.domain.enums.PipelineStage;
import com.totvs.wedjat.infrastructure.db.ConnectionFactory;
import com.totvs.wedjat.infrastructure.db.SchemaValidator;
import com.totvs.wedjat.infrastructure.db.dao.OpportunityDAO;
import com.totvs.wedjat.service.impl.HandoffScoreServiceImpl;
import com.totvs.wedjat.service.impl.OpportunityServiceImpl;
import com.totvs.wedjat.service.impl.SpicedServiceImpl;
import com.totvs.wedjat.service.impl.TranscriptionServiceImpl;

import java.sql.Connection;
import java.util.List;

/**
 * Bateria de testes de integração (conexão, schema e CRUD).
 *
 * <p>Maven: {@code mvn -Ptest-dao exec:java}
 */
public final class TesteDao {

    private static int passed;
    private static int failed;

    private static final OpportunityServiceImpl opportunityService = new OpportunityServiceImpl();
    private static final SpicedServiceImpl spicedService = new SpicedServiceImpl();
    private static final TranscriptionServiceImpl transcriptionService = new TranscriptionServiceImpl();
    private static final HandoffScoreServiceImpl handoffService = new HandoffScoreServiceImpl();

    public static void main(String[] args) {
        titulo("WEDJAT — BATERIA DE TESTES");

        run("Conexão JDBC", TesteDao::testarConexao);
        if (!run("Schema Oracle (sequences + tabelas)", TesteDao::testarSchema)) {
            resumo();
            System.exit(1);
        }

        Opportunity[] inserted = new Opportunity[1];
        if (run("INSERT oportunidade + SPICED", () -> inserted[0] = testarInsert())) {
            final Opportunity ref = inserted[0];
            run("SELECT listar oportunidades", TesteDao::testarFindAll);
            run("SELECT por ID", () -> testarFindById(ref.getId()));
            run("UPDATE etapa pipeline", () -> testarUpdateStage(ref.getId()));
            run("UPDATE SPICED", () -> testarSpiced(ref));
            run("INSERT reunião + insights", () -> testarReuniao(ref));
            run("INSERT/UPDATE handoff score", () -> testarHandoff(ref));
            run("DELETE oportunidade (cascade)", () -> testarDelete(ref.getId()));
            run("SELECT pós-delete", TesteDao::testarFindAll);
        }

        titulo("FIM DA BATERIA");
        resumo();
        System.exit(failed > 0 ? 1 : 0);
    }

    private static boolean run(String nome, ThrowingRunnable test) {
        secao(nome);
        try {
            test.run();
            passed++;
            return true;
        } catch (Exception e) {
            failed++;
            System.out.println("❌ FALHOU: " + rootMessage(e));
            if (e.getCause() != null && e.getCause() != e) {
                System.out.println("   Causa: " + e.getCause().getMessage());
            }
            return false;
        }
    }

    private static void testarConexao() throws Exception {
        try (Connection conn = ConnectionFactory.getConnection()) {
            System.out.println("✅ Conectado: " + conn.getMetaData().getDatabaseProductName()
                    + " | usuário: " + conn.getMetaData().getUserName());
        }
    }

    private static void testarSchema() throws Exception {
        List<String> missing = SchemaValidator.findMissingObjects();
        if (!missing.isEmpty()) {
            System.out.println("❌ Objetos ausentes no schema:");
            missing.forEach(m -> System.out.println("   - " + m));
            System.out.println();
            System.out.println("Correção: conecte no Oracle (SQL Developer / SQL*Plus) com o mesmo");
            System.out.println("usuário do .env e execute o arquivo sql/ddl.sql deste projeto.");
            throw new IllegalStateException("Schema incompleto — " + missing.size() + " objeto(s) faltando");
        }
        System.out.println("✅ Sequences e tabelas OK (5 + 5)");
    }

    private static Opportunity testarInsert() {
        Opportunity opp = opportunityService.cadastrar(
                "Empresa Teste LTDA",
                "Protheus",
                BusinessUnit.TOTVS_GESTAO,
                PipelineStage.QUALIFICACAO);
        System.out.println("✅ ID oportunidade: " + opp.getId()
                + " | SPICED id: " + opp.getSpicedAssessment().getId());
        return opp;
    }

    private static void testarFindAll() {
        List<Opportunity> lista = opportunityService.listar();
        System.out.println("✅ Registros listados: " + lista.size());
    }

    private static void testarFindById(long id) {
        Opportunity o = opportunityService.buscarPorId(id)
                .orElseThrow(() -> new IllegalStateException("Oportunidade id=" + id + " não encontrada"));
        System.out.println("✅ Cliente: " + o.getClientName()
                + " | SPICED: " + o.getSpicedAssessment().countPreenchidos() + "/5");
    }

    private static void testarUpdateStage(long id) {
        if (!opportunityService.atualizarEtapa(id, PipelineStage.PRE_VENDAS)) {
            throw new IllegalStateException("UPDATE etapa não afetou linhas");
        }
        System.out.println("✅ Etapa atualizada para Pré-vendas");
    }

    private static void testarSpiced(Opportunity opp) {
        spicedService.atualizarSpiced(opp,
                "Cliente usa ERP legado",
                "Retrabalho no fechamento",
                "Perda de produtividade",
                "Auditoria em dezembro",
                "CFO e TI");
        if (opp.getSpicedAssessment().countPreenchidos() < 5) {
            throw new IllegalStateException("SPICED deveria ter 5 campos preenchidos");
        }
        System.out.println("✅ SPICED 5/5 persistido");
    }

    private static void testarReuniao(Opportunity opp) {
        String texto = "Cliente usa Protheus, dor em RH, comparou com Senior, CFO bloqueou orçamento";
        MeetingRecord meeting = transcriptionService.registrarReuniao(opp, texto);
        if (meeting.getId() == null) {
            throw new IllegalStateException("Reunião sem ID após insert");
        }
        System.out.println("✅ Reunião id=" + meeting.getId()
                + " | insights=" + meeting.getInsights().size());
    }

    private static void testarHandoff(Opportunity opp) {
        String relatorio = handoffService.gerarRelatorio(opp);
        if (relatorio == null || !relatorio.contains("HANDOFF SCORE")) {
            throw new IllegalStateException("Relatório de handoff inválido");
        }
        System.out.println("✅ Handoff persistido (trecho): "
                + relatorio.lines().filter(l -> l.contains("Score:")).findFirst().orElse("ok"));
    }

    private static void testarDelete(long id) {
        if (!new OpportunityDAO().delete(id)) {
            throw new IllegalStateException("DELETE não removeu oportunidade id=" + id);
        }
        System.out.println("✅ DELETE id=" + id + " (cascade em filhos)");
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }

    private static void titulo(String texto) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  " + texto);
        System.out.println("=".repeat(50));
    }

    private static void secao(String texto) {
        System.out.println("\n--- " + texto + " ---");
    }

    private static void resumo() {
        System.out.println("\nResumo: " + passed + " OK | " + failed + " FALHA(S)");
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
