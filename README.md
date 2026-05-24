# Wedjat — Inteligência Comercial TOTVS

Aplicação Java de console para o Challenge FIAP x TOTVS. Transforma dados de oportunidades comerciais, metodologia SPICED e transcrições de reuniões em insights acionáveis para vendedores e gestores de BU.

Sem framework e sem gerenciador de dependências — apenas Java puro com `Scanner`.

## Arquitetura em camadas

```
com.totvs.wedjat
├── Main.java                 → ponto de entrada
├── console/                  → interação com usuário (Scanner)
│   ├── MenuConsole           → loop do menu e roteamento
│   ├── ConsoleMenuHandler    → ações de cada opção do menu
│   └── ConsoleInput          → leitura e validação de entrada
├── application/              → orquestração dos fluxos
│   └── WedjatSystem
├── domain/                   → entidades de domínio
│   ├── Opportunity
│   ├── MeetingRecord
│   ├── SpicedAssessment
│   ├── HandoffScore
│   ├── Insight               → classe abstrata (herança)
│   │   ├── RiskInsight
│   │   ├── OpportunityInsight
│   │   ├── ChurnInsight
│   │   ├── CompetitorInsight
│   │   └── CrossSellInsight
│   ├── InsightFactory        → criação polimórfica de insights
│   └── enums/                → tipos enumerados
│       ├── BusinessUnit
│       ├── PipelineStage
│       └── InsightType
└── service/                  → contratos (interfaces)
    ├── IOpportunityService
    ├── ISpicedService
    ├── ITranscriptionService
    ├── IHandoffScoreService
    ├── IDashboardService
    └── impl/                 → implementações
        ├── OpportunityServiceImpl
        ├── SpicedServiceImpl
        ├── TranscriptionServiceImpl
        ├── HandoffScoreServiceImpl
        └── DashboardServiceImpl
```

### Responsabilidades

| Pacote | Responsabilidade |
|--------|------------------|
| `console` | Menu, leitura de entrada e exibição de saída |
| `application` | Fachada que coordena serviços e dados em memória |
| `domain` | Entidades, herança (`Insight` ← subclasses), referências entre objetos |
| `domain.enums` | Enums de BU, etapa do pipeline e tipo de insight |
| `service` | Interfaces dos serviços de negócio |
| `service.impl` | Implementações concretas dos serviços |

## Como executar

Requisito: **Java 11+**

```bash
mkdir -p out
find src -name "*.java" -print0 | xargs -0 javac -d out -encoding UTF-8
java -cp out com.totvs.wedjat.Main
```

Ou abra o projeto na IDE com **source root** em `src` e execute `com.totvs.wedjat.Main`.

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
- **Herança e polimorfismo**: subclasses de `Insight` com `@Override` em `getType()` e `getPrioridade()`; listas tipadas como `List<Insight>`
- **Factory**: `InsightFactory.criar(...)` instancia a subclasse correta conforme `InsightType`
- **Atributo de referência**: `Opportunity` → `HandoffScore`; `HandoffScore` → `Opportunity`
- **Interfaces + implementações**: camada `service` / `service.impl` (ex.: `IHandoffScoreService` ← `HandoffScoreServiceImpl`)

## Funcionalidades de negócio

- **SPICED Auto-Fill**: preenchimento parcial a partir de transcrições
- **Impact Gap Alert**: alerta quando há dor sem impacto quantificado
- **Análise de transcrição**: detecta produto TOTVS, concorrente, churn, risco e cross-sell
- **Handoff Score**: mede qualidade da passagem pré-vendas → vendas
- **Dashboard por BU**: visão consolidada de oportunidades e riscos

## Exemplo de fluxo

1. Cadastre uma oportunidade (opção 1)
2. Registre uma transcrição (opção 3) com texto como:
   > "Cliente usa Protheus, tem dor em RH, comparou com Senior, CFO bloqueou orçamento"
3. Gere o briefing SPICED (opção 5)
4. Calcule o handoff score (opção 6)
5. Veja o dashboard (opção 7)
