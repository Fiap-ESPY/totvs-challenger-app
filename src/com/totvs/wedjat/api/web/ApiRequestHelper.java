package com.totvs.wedjat.api.web;

import java.util.Map;

final class ApiRequestHelper {

    private ApiRequestHelper() {
    }

    static long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Id da oportunidade inválido.");
        }
    }

    static String texto(Map<String, Object> body, String campo) {
        if (body == null) {
            return null;
        }
        Object valor = body.get(campo);
        if (valor == null) {
            return null;
        }
        return String.valueOf(valor);
    }
}
