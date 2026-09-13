-- ============================================================
-- Wedjat — Inteligência Comercial TOTVS
-- DDL Oracle — criação de tabelas, sequences e constraints
-- ============================================================

-- ============================================================
-- SEQUENCES (IDs sequenciais — padrão Oracle)
-- ============================================================

CREATE SEQUENCE SQ_OPPORTUNITY  START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SQ_SPICED       START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SQ_MEETING      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SQ_INSIGHT      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SQ_HANDOFF      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- ============================================================
-- TB_OPPORTUNITY
-- ============================================================

CREATE TABLE TB_OPPORTUNITY (
    id             NUMBER          CONSTRAINT pk_opportunity PRIMARY KEY,
    client_name    VARCHAR2(200)   CONSTRAINT nn_opp_client  NOT NULL,
    product        VARCHAR2(200)   CONSTRAINT nn_opp_product NOT NULL,
    business_unit  VARCHAR2(50)    CONSTRAINT nn_opp_bu      NOT NULL,
    pipeline_stage VARCHAR2(50)    CONSTRAINT nn_opp_stage   NOT NULL
);

COMMENT ON TABLE  TB_OPPORTUNITY               IS 'Oportunidades comerciais monitoradas pelo Wedjat.';
COMMENT ON COLUMN TB_OPPORTUNITY.id            IS 'Identificador gerado pela SEQUENCE SQ_OPPORTUNITY.';
COMMENT ON COLUMN TB_OPPORTUNITY.client_name   IS 'Nome do cliente da oportunidade.';
COMMENT ON COLUMN TB_OPPORTUNITY.product       IS 'Produto TOTVS relacionado à oportunidade.';
COMMENT ON COLUMN TB_OPPORTUNITY.business_unit IS 'BU responsável. Valores: TOTVS_GESTAO | RD_STATION | TECHFIN.';
COMMENT ON COLUMN TB_OPPORTUNITY.pipeline_stage IS 'Etapa do pipeline. Valores: GERACAO_DEMANDA | QUALIFICACAO | PRE_VENDAS | REUNIAO_DIAGNOSTICA | PROPOSTA | NEGOCIACAO | FECHAMENTO.';

-- ============================================================
-- TB_SPICED_ASSESSMENT (1:1 com TB_OPPORTUNITY)
-- ============================================================

CREATE TABLE TB_SPICED_ASSESSMENT (
    id             NUMBER          CONSTRAINT pk_spiced      PRIMARY KEY,
    id_opportunity NUMBER          CONSTRAINT nn_spiced_opp  NOT NULL
                                   CONSTRAINT uq_spiced_opp  UNIQUE,
    situation      VARCHAR2(1000),
    pain           VARCHAR2(1000),
    impact         VARCHAR2(1000),
    critical_event VARCHAR2(1000),
    decision       VARCHAR2(1000),
    CONSTRAINT fk_spiced_opportunity
        FOREIGN KEY (id_opportunity) REFERENCES TB_OPPORTUNITY(id) ON DELETE CASCADE
);

COMMENT ON TABLE  TB_SPICED_ASSESSMENT              IS 'Avaliação SPICED vinculada a uma oportunidade (relação 1:1).';
COMMENT ON COLUMN TB_SPICED_ASSESSMENT.id           IS 'Identificador gerado pela SEQUENCE SQ_SPICED.';
COMMENT ON COLUMN TB_SPICED_ASSESSMENT.id_opportunity IS 'FK para TB_OPPORTUNITY. Unique — cada oportunidade tem um único SPICED.';
COMMENT ON COLUMN TB_SPICED_ASSESSMENT.situation    IS 'S — Situação atual do cliente (contexto, sistemas em uso).';
COMMENT ON COLUMN TB_SPICED_ASSESSMENT.pain         IS 'P — Dor ou problema operacional/estratégico identificado.';
COMMENT ON COLUMN TB_SPICED_ASSESSMENT.impact       IS 'I — Impacto financeiro ou de negócio da dor.';
COMMENT ON COLUMN TB_SPICED_ASSESSMENT.critical_event IS 'C — Evento crítico com prazo que exige decisão.';
COMMENT ON COLUMN TB_SPICED_ASSESSMENT.decision     IS 'E(D) — Decisores e processo de aprovação.';

-- ============================================================
-- TB_MEETING_RECORD (N:1 com TB_OPPORTUNITY)
-- ============================================================

CREATE TABLE TB_MEETING_RECORD (
    id             NUMBER          CONSTRAINT pk_meeting      PRIMARY KEY,
    id_opportunity NUMBER          CONSTRAINT nn_meeting_opp  NOT NULL,
    meeting_date   DATE            CONSTRAINT nn_meeting_date NOT NULL,
    transcription  CLOB            CONSTRAINT nn_meeting_tr   NOT NULL,
    CONSTRAINT fk_meeting_opportunity
        FOREIGN KEY (id_opportunity) REFERENCES TB_OPPORTUNITY(id) ON DELETE CASCADE
);

COMMENT ON TABLE  TB_MEETING_RECORD                IS 'Registros de reuniões comerciais vinculadas a oportunidades.';
COMMENT ON COLUMN TB_MEETING_RECORD.id             IS 'Identificador gerado pela SEQUENCE SQ_MEETING.';
COMMENT ON COLUMN TB_MEETING_RECORD.id_opportunity IS 'FK para TB_OPPORTUNITY. Uma oportunidade pode ter várias reuniões.';
COMMENT ON COLUMN TB_MEETING_RECORD.meeting_date   IS 'Data em que a reunião ocorreu.';
COMMENT ON COLUMN TB_MEETING_RECORD.transcription  IS 'Texto completo da transcrição da reunião (CLOB — sem limite prático).';

-- ============================================================
-- TB_INSIGHT (N:1 com TB_MEETING_RECORD)
-- ============================================================

CREATE TABLE TB_INSIGHT (
    id          NUMBER          CONSTRAINT pk_insight     PRIMARY KEY,
    id_meeting  NUMBER          CONSTRAINT nn_ins_meeting NOT NULL,
    type        VARCHAR2(50)    CONSTRAINT nn_ins_type    NOT NULL,
    description VARCHAR2(1000)  CONSTRAINT nn_ins_desc    NOT NULL,
    evidence    VARCHAR2(500),
    CONSTRAINT fk_insight_meeting
        FOREIGN KEY (id_meeting) REFERENCES TB_MEETING_RECORD(id) ON DELETE CASCADE
);

COMMENT ON TABLE  TB_INSIGHT             IS 'Insights extraídos automaticamente das transcrições de reuniões.';
COMMENT ON COLUMN TB_INSIGHT.id          IS 'Identificador gerado pela SEQUENCE SQ_INSIGHT.';
COMMENT ON COLUMN TB_INSIGHT.id_meeting  IS 'FK para TB_MEETING_RECORD. Um insight pertence a uma reunião.';
COMMENT ON COLUMN TB_INSIGHT.type        IS 'Tipo do insight. Valores: OPORTUNIDADE | RISCO | CONCORRENTE | CHURN | CROSS_SELL.';
COMMENT ON COLUMN TB_INSIGHT.description IS 'Descrição do insight gerado pela análise.';
COMMENT ON COLUMN TB_INSIGHT.evidence    IS 'Trecho ou fonte que embasou o insight (ex: "transcrição").';

-- ============================================================
-- TB_HANDOFF_SCORE (1:1 com TB_OPPORTUNITY)
-- ============================================================

CREATE TABLE TB_HANDOFF_SCORE (
    id             NUMBER          CONSTRAINT pk_handoff      PRIMARY KEY,
    id_opportunity NUMBER          CONSTRAINT nn_hf_opp       NOT NULL
                                   CONSTRAINT uq_hf_opp       UNIQUE,
    score          NUMBER(3)       CONSTRAINT nn_hf_score     NOT NULL,
    classificacao  VARCHAR2(200)   CONSTRAINT nn_hf_class     NOT NULL,
    campos_spiced  NUMBER(1)       CONSTRAINT nn_hf_spiced    NOT NULL,
    CONSTRAINT ck_handoff_score    CHECK (score BETWEEN 0 AND 100),
    CONSTRAINT ck_handoff_spiced   CHECK (campos_spiced BETWEEN 0 AND 5),
    CONSTRAINT fk_handoff_opportunity
        FOREIGN KEY (id_opportunity) REFERENCES TB_OPPORTUNITY(id) ON DELETE CASCADE
);

COMMENT ON TABLE  TB_HANDOFF_SCORE               IS 'Score de qualidade do handoff pré-vendas → vendas por oportunidade (relação 1:1).';
COMMENT ON COLUMN TB_HANDOFF_SCORE.id            IS 'Identificador gerado pela SEQUENCE SQ_HANDOFF.';
COMMENT ON COLUMN TB_HANDOFF_SCORE.id_opportunity IS 'FK para TB_OPPORTUNITY. Unique — cada oportunidade tem um único handoff score.';
COMMENT ON COLUMN TB_HANDOFF_SCORE.score         IS 'Pontuação de 0 a 100.';
COMMENT ON COLUMN TB_HANDOFF_SCORE.classificacao IS 'Classificação textual do score (ex: "Handoff de alta qualidade").';
COMMENT ON COLUMN TB_HANDOFF_SCORE.campos_spiced IS 'Quantidade de campos SPICED preenchidos (0 a 5) no momento do cálculo.';
