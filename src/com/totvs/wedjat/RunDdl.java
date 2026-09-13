package com.totvs.wedjat;

import com.totvs.wedjat.infrastructure.db.ConnectionFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Aplica {@code sql/ddl.sql} no schema Oracle configurado no {@code .env}.
 * Uso: {@code mvn -Prun-ddl exec:java} ou {@code mvn exec:java -Dexec.mainClass=com.totvs.wedjat.RunDdl}
 */
public final class RunDdl {

    public static void main(String[] args) throws Exception {
        Path ddl = Path.of("sql", "ddl.sql");
        if (!Files.isRegularFile(ddl)) {
            System.err.println("Arquivo não encontrado: " + ddl.toAbsolutePath());
            System.exit(1);
        }

        List<String> statements = parseStatements(Files.readString(ddl));
        System.out.println("Executando " + statements.size() + " comando(s) DDL...");

        int ok = 0;
        int skipped = 0;
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : statements) {
                try {
                    stmt.execute(sql);
                    ok++;
                    System.out.println("OK: " + resumo(sql));
                } catch (SQLException e) {
                    if (alreadyExists(e)) {
                        skipped++;
                        System.out.println("SKIP (já existe): " + resumo(sql));
                    } else {
                        System.err.println("ERRO em: " + resumo(sql));
                        throw e;
                    }
                }
            }
        }

        System.out.println("DDL concluído: " + ok + " aplicado(s), " + skipped + " ignorado(s).");
    }

    private static List<String> parseStatements(String script) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String line : script.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            current.append(line).append('\n');
            if (trimmed.endsWith(";")) {
                String sql = current.toString().trim();
                if (sql.endsWith(";")) {
                    sql = sql.substring(0, sql.length() - 1);
                }
                result.add(sql);
                current.setLength(0);
            }
        }
        return result;
    }

    private static boolean alreadyExists(SQLException e) {
        int code = e.getErrorCode();
        return code == 955 || code == 2260 || code == 2275 || code == 1408;
    }

    private static String resumo(String sql) {
        String oneLine = sql.lines().findFirst().orElse(sql).trim();
        if (oneLine.length() > 70) {
            return oneLine.substring(0, 67) + "...";
        }
        return oneLine;
    }
}
