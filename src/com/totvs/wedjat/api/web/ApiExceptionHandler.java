package com.totvs.wedjat.api.web;

import com.totvs.wedjat.infrastructure.db.DbExceptionMessages;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarValidacao(IllegalArgumentException erro) {
        String mensagem = erro.getMessage() == null ? "Requisicao invalida." : erro.getMessage();
        HttpStatus status = mensagem.contains("nao encontrada") || mensagem.contains("não encontrada")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> tratarGeral(Exception erro) {
        if (erro instanceof IllegalArgumentException) {
            return tratarValidacao((IllegalArgumentException) erro);
        }
        if (temSqlException(erro)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", DbExceptionMessages.format(erro)));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Falha interna ao processar a requisicao."));
    }

    private static boolean temSqlException(Throwable erro) {
        Throwable atual = erro;
        while (atual != null) {
            if (atual instanceof SQLException) {
                return true;
            }
            atual = atual.getCause();
        }
        return false;
    }
}
