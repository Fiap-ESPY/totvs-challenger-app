# Rotas da API Wedjat

Sobe a API e o front na mesma porta. O menu de console continua em `mvn exec:java`.

```bash
mvn -Prun-api exec:java
```

Abra `http://localhost:7070`.

Códigos usados: **200** sucesso, **201** criado, **400** validação, **404** id inexistente, **500** falha de banco. O corpo de erro é `{ "erro": "mensagem" }`, sem stack trace.

## Oportunidades

`POST /oportunidades` → 201

```json
{
  "cliente": "Indústria Aurora",
  "produto": "Protheus",
  "bu": "TOTVS_GESTAO",
  "etapa": "QUALIFICACAO"
}
```

`GET /oportunidades` → 200, lista.

`GET /oportunidades/{id}` → 200 detalhe, ou 404.

`PUT /oportunidades/{id}` → 200. Atualiza cliente, produto e BU. A etapa não muda.

```json
{
  "cliente": "Indústria Aurora S.A.",
  "produto": "RM",
  "bu": "RD_STATION"
}
```

`PATCH /oportunidades/{id}/etapa` → 200

```json
{ "etapa": "PRE_VENDAS" }
```

`DELETE /oportunidades/{id}` → 200 `{ "mensagem": "Oportunidade excluída." }`

BU aceita `TOTVS_GESTAO`, `RD_STATION`, `TECHFIN` (ou 1, 2, 3). Etapa aceita o nome do enum ou 1 a 7.

## Reuniões

`GET /oportunidades/{id}/reunioes` → 200

`POST /oportunidades/{id}/reunioes` → 201

```json
{
  "transcricao": "Cliente usa Protheus, tem dor em RH, comparou com Senior e o CFO bloqueou o orçamento."
}
```

## SPICED e briefing

`GET /oportunidades/{id}/spiced` → 200

`PUT /oportunidades/{id}/spiced` → 200. Campo em branco mantém o valor já gravado.

```json
{
  "situation": "ERP legado no fechamento",
  "pain": "Retrabalho manual",
  "impact": "Perda de produtividade no financeiro",
  "criticalEvent": "Auditoria em dezembro",
  "decision": "CFO e TI"
}
```

`GET /oportunidades/{id}/briefing` → 200 `{ "briefing": "...", "spiced": { } }`

## Handoff e dashboard

`POST /oportunidades/{id}/handoff` → 200 `{ "score": 80, "classificacao": "...", "relatorio": "..." }`

`GET /dashboard` → 200

`GET /dashboard/{bu}` → 200, por exemplo `GET /dashboard/TOTVS_GESTAO`
