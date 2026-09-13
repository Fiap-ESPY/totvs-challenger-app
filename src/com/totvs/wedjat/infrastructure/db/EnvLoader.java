package com.totvs.wedjat.infrastructure.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Carrega variáveis de configuração a partir de um arquivo {@code .env}
 * localizado na raiz do projeto.
 */
final class EnvLoader {

    private static final String ENV_FILE = ".env";
    private static final Properties props = new Properties();

    static {
        File file = new File(ENV_FILE);
        if (!file.exists()) {
            throw new ExceptionInInitializerError(
                    "Arquivo .env não encontrado na raiz do projeto. " +
                    "Copie .env.example para .env e preencha as credenciais.");
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                    "Erro ao ler o arquivo .env: " + e.getMessage());
        }
    }

    private EnvLoader() {
    }

    /**
     * Retorna o valor da chave informada.
     *
     * @param key chave presente no {@code .env}
     * @return valor correspondente
     * @throws IllegalStateException se a chave não for encontrada ou estiver vazia
     */
    static String get(String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Variável '" + key + "' não encontrada ou vazia no arquivo .env.");
        }
        return value.trim();
    }
}
