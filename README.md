# Wedjat — Inteligência Comercial TOTVS

Aplicação Java de console para o Challenge FIAP x TOTVS. Transforma dados de oportunidades comerciais, metodologia SPICED e transcrições de reuniões em insights acionáveis para vendedores e gestores de BU.

Persistência em **Oracle** via **JDBC** (driver `ojdbc11` gerenciado pelo **Maven**). Interface de uso: menu no terminal (`Scanner`).

## Requisitos

| Ferramenta | Versão |
|------------|--------|
| Java | 21 ([`.sdkmanrc`](.sdkmanrc) → `21.0.2-open`) |
| Maven | 3.9+ |
| Oracle | Schema FIAP (ex.: `oracle.fiap.com.br`) |

## Configuração do banco

1. Copie o template de credenciais:

```bash
cp .env.example .env
```

2. Edite `.env` (arquivo **não** versionado):

```env
DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl
DB_USER=SEU_USUARIO
DB_PASSWORD=SUA_SENHA
```

3. Na **primeira vez** (ou em schema vazio), crie sequences e tabelas:

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

### Bateria de testes (integração JDBC/DAO)

Valida conexão, schema e fluxo CRUD ponta a ponta:

```bash
mvn -Ptest-dao exec:java
```

Saída esperada no final: `Resumo: 11 OK | 0 FALHA(S)`.

## Arquitetura em camadas

```
com.totvs.wedjat
├── Main.java
├── TesteDao.java             → bateria de testes de integração
├── RunDdl.java               → aplica sql/ddl.sql no Oracle
├── console/                  → menu e entrada do usuário
├── application/              → WedjatSystem (fachada)
├── domain/                   → entidades, enums, Insight (+ subclasses)
├── service/                  → interfaces
│   └── impl/                 → regras de negócio + persistência via DAO
└── infrastructure/db/
    ├── ConnectionFactory     → JDBC (credenciais via .env)
    ├── EnvLoader
    ├── SchemaValidator
    ├── DbExceptionMessages
    └── dao/                  → CRUD Oracle
        ├── OpportunityDAO
        ├── SpicedAssessmentDAO
        ├── MeetingRecordDAO
        ├── InsightDAO
        └── HandoffScoreDAO
```

### Responsabilidades

| Pacote | Responsabilidade |
|--------|------------------|
| `console` | Menu, leitura de entrada e exibição de saída |
| `application` | Orquestra os serviços expostos ao menu |
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

1. Cadastre uma oportunidade (opção **1**)
2. Registre uma transcrição (opção **3**), por exemplo:
   > "Cliente usa Protheus, tem dor em RH, comparou com Senior, CFO bloqueou orçamento"
3. Gere o briefing SPICED (opção **5**)
4. Calcule o handoff score (opção **6**)
5. Veja o dashboard (opção **7**)

## Perfis Maven (`pom.xml`)

| Comando | Profile | Descrição |
|---------|---------|-----------|
| `mvn exec:java` | (padrão) | Sobe o Wedjat (menu console) |
| `mvn -Prun-ddl exec:java` | `run-ddl` | Aplica `sql/ddl.sql` no Oracle |
| `mvn -Ptest-dao exec:java` | `test-dao` | Bateria de testes JDBC/DAO |

## Problemas comuns

| Mensagem | O que fazer |
|----------|-------------|
| Arquivo `.env` não encontrado | `cp .env.example .env` e preencha as variáveis |
| ORA-02289 (sequência não existe) | Rode `mvn -Prun-ddl exec:java` |
| ORA-00942 (tabela não existe) | Idem — aplicar DDL |
| Driver Oracle não encontrado | Use Maven (`mvn compile`), não compile só com `javac` sem classpath do `ojdbc11` |
