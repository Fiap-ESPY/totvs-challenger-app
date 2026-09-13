package com.totvs.wedjat.infrastructure.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexões JDBC com o banco Oracle da FIAP.
 * As credenciais estão definidas como constantes nesta classe,
 * conforme exigido pela disciplina DDD — Sprint 3.
 */
public final class ConnectionFactory {

    private static final String URL      = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl";
    private static final String USER     = "RM563986";
    private static final String PASSWORD = "290800";

    private ConnectionFactory() {
    }

    /**
     * Abre e retorna uma conexão com o banco de dados Oracle.
     * O chamador é responsável por fechar a conexão após o uso.
     *
     * @return {@link Connection} aberta.
     * @throws SQLException se o driver não for encontrado ou a conexão falhar.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver Oracle (ojdbc) não encontrado no classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
