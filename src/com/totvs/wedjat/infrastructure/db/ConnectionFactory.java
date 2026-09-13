package com.totvs.wedjat.infrastructure.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexões JDBC. As credenciais são lidas do arquivo {@code .env}
 * na raiz do projeto — nunca hardcoded no código-fonte.
 *
 * <p>Chaves esperadas no {@code .env}:
 * <pre>
 *   DB_URL=jdbc:oracle:thin:@...
 *   DB_USER=...
 *   DB_PASSWORD=...
 * </pre>
 */
public final class ConnectionFactory {

    private ConnectionFactory() {
    }

    /**
     * Abre e retorna uma conexão com o banco de dados.
     * O chamador é responsável por fechar a conexão após o uso.
     *
     * @return {@link Connection} aberta.
     * @throws SQLException se o driver não for encontrado ou a conexão falhar.
     */
    public static Connection getConnection() throws SQLException {
        String url      = EnvLoader.get("DB_URL");
        String user     = EnvLoader.get("DB_USER");
        String password = EnvLoader.get("DB_PASSWORD");

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver Oracle (ojdbc) não encontrado no classpath.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }
}
