# Wedjat — Inteligência Comercial TOTVS

Solução Java do Challenge FIAP x TOTVS para apoiar pré-vendas e vendas com oportunidades comerciais, metodologia SPICED, transcrições de reunião e dashboard por BU.

Persistência em **Oracle** via **JDBC** (`ojdbc11`, Maven). Três formas de uso:

- **Console** (Sprint 3): menu no terminal — `mvn exec:java`
- **API REST + web** (Sprint 4): Javalin na porta 7070 e front estático em [`web/`](web/) — `mvn -Prun-api exec:java`

## Requisitos

| Ferramenta | Versão |
|------------|--------|
| Java | 21 ([`.sdkmanrc`](.sdkmanrc) → `21.0.2-open`) |
| Maven | 3.9+ |
| Oracle | Schema FIAP (ex.: `oracle.fiap.com.br`) |

## Configuração do banco

A conexão JDBC está em [`ConnectionFactory.java`](src/com/totvs/wedjat/infrastructure/db/ConnectionFactory.java), com **URL, usuário e senha no código**, conforme a rubrica da Sprint 3 de DDD. Antes de rodar no seu schema FIAP, ajuste `URL`, `USER` e `PASSWORD` nessa classe.

O arquivo [`.env.example`](.env.example) é apenas referência de formato; o runtime **não** lê `.env` hoje.

Na **primeira vez** (ou em schema vazio), crie sequences e tabelas:

```bash
sdk env
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.2-open"
mvn -Prun-ddl exec:java
```

O script aplicado é [`sql/ddl.sql`](sql/ddl.sql) (5 sequences + 5 tabelas).

## Como executar

Com [SDKMAN](https://sdkman.io/) na raiz do repositório:

```bash
sdk env
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.2-open"
mvn compile exec:java
```

Classe principal: `com.totvs.wedjat.Main`.

### API REST + telas (Sprint 4)

```bash
sdk env
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.2-open"
mvn -Prun-api exec:java
```

Abra [http://localhost:7070](http://localhost:7070). Rotas documentadas em [`docs/rotas.md`](docs/rotas.md). PDF da entrega: [`wedjat-sprint4.html`](wedjat-sprint4.html) (Imprimir → Salvar como PDF).

### Bateria de testes (integração JDBC/DAO)

Valida conexão, schema e fluxo CRUD ponta a ponta (pode levar alguns minutos por causa do Oracle remoto):

```bash
sdk env
export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.2-open"
mvn -Ptest-dao exec:java
```

Saída esperada no final: `Resumo: 14 OK | 0 FALHA(S)`.

## Arquitetura em camadas

```
com.totvs.wedjat
├── Main.java
├── TesteDao.java             → bateria de testes de integração
├── RunDdl.java               → aplica sql/ddl.sql no Oracle
├── console/                  → menu e entrada do usuário
├── application/              → WedjatSystem (fachada)
├── api/                      → ApiServer, JsonView (REST Sprint 4)
├── domain/                   → entidades, enums, Insight (+ subclasses)
├── service/                  → interfaces
│   └── impl/                 → regras de negócio + persistência via DAO
└── infrastructure/db/
    ├── ConnectionFactory     → JDBC (URL/usuário/senha na classe)
    ├── SchemaValidator
    ├── DbExceptionMessages
    └── dao/                  → CRUD Oracle
        ├── OpportunityDAO
        ├── SpicedAssessmentDAO
        ├── MeetingRecordDAO
        ├── InsightDAO
        └── HandoffScoreDAO

web/                          → front Sprint 4 (HTML/CSS/JS), servido pela API
docs/                         → rotas REST, prints da documentação
sql/ddl.sql                   → DDL Oracle
```

### Responsabilidades

| Pacote / pasta | Responsabilidade |
|----------------|------------------|
| `console` | Menu, leitura de entrada e exibição de saída |
| `application` | Fachada `WedjatSystem` (console e API) |
| `api` | Rotas REST, JSON e tratamento de erros HTTP |
| `web/` | Telas que consomem a API no navegador |
| `domain` | Entidades, herança (`Insight` ← subclasses), regras no domínio |
| `service` / `service.impl` | Casos de uso (SPICED, transcrição, handoff, dashboard) |
| `infrastructure.db.dao` | Persistência JDBC (INSERT, SELECT, UPDATE, DELETE) |

### Modelo relacional (Oracle)

| Tabela | Descrição |
|--------|-----------|
| `TB_OPPORTUNITY` | Oportunidade comercial |
| `TB_SPICED_ASSESSMENT` | SPICED (1:1 com oportunidade) |
| `TB_MEETING_RECORD` | Reuniões / transcrições |
| `TB_INSIGHT` | Insights extraídos da transcrição |
| `TB_HANDOFF_SCORE` | Score de handoff (1:1 com oportunidade) |

IDs gerados por sequences (`SQ_OPPORTUNITY`, `SQ_SPICED`, etc.).

## Menu do sistema

| Opção | Ação |
|------:|------|
| **1** | Cadastrar oportunidade (cliente, produto, BU, etapa) |
| **2** | Listar oportunidades |
| **3** | Registrar reunião/transcrição e extrair insights |
| **4** | Atualizar campos SPICED manualmente |
| **5** | Gerar briefing SPICED com lacunas e perguntas |
| **6** | Calcular handoff score |
| **7** | Ver dashboard geral |
| **8** | Ver dashboard por BU |
| **9** | Atualizar etapa do pipeline |
| **0** | Sair |

## Recursos orientados a objetos

- **5 entidades de domínio**: `Opportunity`, `MeetingRecord`, `SpicedAssessment`, `Insight` (abstrata) e `HandoffScore`
- **Herança e polimorfismo**: subclasses de `Insight` com `@Override` em `getType()` e `getPrioridade()`
- **Factory**: `InsightFactory.criar(...)` e `InsightFactory.carregar(...)` (reconstrução a partir do banco)
- **Interfaces + implementações**: `service` / `service.impl` + DAOs JDBC

## Funcionalidades de negócio

- **SPICED Auto-Fill**: preenchimento parcial a partir de transcrições
- **Impact Gap Alert**: alerta quando há dor sem impacto quantificado
- **Análise de transcrição**: detecta produto TOTVS, concorrente, churn, risco e cross-sell
- **Handoff Score**: mede qualidade da passagem pré-vendas → vendas
- **Dashboard por BU**: visão consolidada de oportunidades e riscos

## Exemplo de fluxo

**Console:** cadastrar oportunidade (**1**) → transcrição (**3**) → briefing SPICED (**5**) → handoff (**6**) → dashboard (**7**).

**Web (Sprint 4):** com `mvn -Prun-api exec:java`, use as abas Oportunidades → Reunião → SPICED → Handoff → Dashboard em [http://localhost:7070](http://localhost:7070).

Transcrição de exemplo:

> Cliente usa Protheus, tem dor em RH, comparou com Senior, CFO bloqueou orçamento

## Perfis Maven (`pom.xml`)

| Comando | Profile | Descrição |
|---------|---------|-----------|
| `mvn exec:java` | (padrão) | Sobe o Wedjat (menu console) |
| `mvn -Prun-api exec:java` | `run-api` | API REST + front em `web/` |
| `mvn -Prun-ddl exec:java` | `run-ddl` | Aplica `sql/ddl.sql` no Oracle |
| `mvn -Ptest-dao exec:java` | `test-dao` | Bateria de testes JDBC/DAO |

## Problemas comuns

| Mensagem | O que fazer |
|----------|-------------|
| `invalid target release: 21` | Use Java 21: `sdk env` e `export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.2-open"` antes do `mvn` |
| ORA-01017 / falha de login | Confira usuário e senha em `ConnectionFactory.java` |
| ORA-02289 (sequência não existe) | Rode `mvn -Prun-ddl exec:java` |
| ORA-00942 (tabela não existe) | Idem — aplicar DDL |
| Driver Oracle não encontrado | Use Maven (`mvn compile`), não compile só com `javac` sem classpath do `ojdbc11` |
| API lenta na listagem | Normal no Oracle FIAP remoto; listagem/dashboard usam consulta resumida (JOIN SPICED) |
