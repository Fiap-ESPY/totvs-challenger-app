package com.totvs.wedjat.infrastructure.db;

import java.sql.SQLException;

/**
 * Mensagens amigáveis para falhas de persistência (JDBC / Oracle).
 */
public final class DbExceptionMessages {

    private DbExceptionMessages() {
    }

    public static String format(Throwable error) {
        SQLException sql = findSqlException(error);
        if (sql != null) {
            return formatOracle(sql);
        }
        Throwable cause = error.getCause();
        if (cause != null && cause != error) {
            return format(cause);
        }
        return "Erro de banco de dados: " + error.getMessage();
    }

    private static String formatOracle(SQLException sql) {
        String code = String.valueOf(sql.getErrorCode());
        String msg = sql.getMessage();
        if (msg != null && msg.contains("\n")) {
            msg = msg.substring(0, msg.indexOf('\n')).trim();
        }

        if ("2289".equals(code) || (msg != null && msg.contains("ORA-02289"))) {
            return "Erro Oracle ORA-02289: sequência não existe. "
                    + "Execute o script sql/ddl.sql no seu schema Oracle antes de usar o sistema.";
        }
        if ("942".equals(code) || (msg != null && msg.contains("ORA-00942"))) {
            return "Erro Oracle ORA-00942: tabela ou view não existe. "
                    + "Execute o script sql/ddl.sql no seu schema Oracle.";
        }
        if (msg != null && !msg.isBlank()) {
            return "Erro Oracle: " + msg;
        }
        return "Erro Oracle (código " + code + ").";
    }

    private static SQLException findSqlException(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SQLException sql) {
                return sql;
            }
            current = current.getCause();
        }
        return null;
    }
}
