-- Gerado por Oracle SQL Developer Data Modeler 23.1.0.087.0806
--   em:        2025-03-11 15:12:42 BRT
--   site:      Oracle Database 11g
--   tipo:      Oracle Database 11g


CREATE TABLE t_mtp_autenticador (
    idt_autenticador NUMBER(10) NOT NULL,
    des_senha        VARCHAR2(10 CHAR) NOT NULL
);

ALTER TABLE t_mtp_autenticador ADD CONSTRAINT t_mtp_autenticador_pk PRIMARY KEY ( idt_autenticador );

CREATE TABLE t_mtp_carteira (
    idt_carteira              NUMBER(5) NOT NULL,
    num_conta                 NUMBER(5) NOT NULL,
    qtd_saldo_criptomoeda     NUMBER(10, 2) NOT NULL,
    valor_atual               NUMBER(10, 2) NOT NULL,
    des_variacao              NUMBER(5, 2) NOT NULL,
    nom_criptomoeda_adquirida VARCHAR2(30) NOT NULL,
    nom_carteira              VARCHAR2(30)
);

ALTER TABLE t_mtp_carteira ADD CONSTRAINT t_mtp_carteira_pk PRIMARY KEY ( idt_carteira );

CREATE TABLE t_mtp_compra (
    cod_compra    NUMBER(3) NOT NULL,
    idt_transacao NUMBER(5) NOT NULL
);

ALTER TABLE t_mtp_compra ADD CONSTRAINT t_mtp_compra_pk PRIMARY KEY ( cod_compra );

CREATE TABLE t_mtp_conta (
    num_conta               NUMBER(5) NOT NULL,
    idt_autenticador1       NUMBER(10) NOT NULL,
    qtd_conta_registrada    NUMBER NOT NULL,
    qtd_reserva_criptoativo NUMBER NOT NULL,
    nom_carteira            VARCHAR2 (50),
    val_saldo_conta         NUMBER(10, 2) NOT NULL
);

ALTER TABLE t_mtp_conta ADD CONSTRAINT t_mtp_conta_pk PRIMARY KEY ( num_conta );

ALTER TABLE t_mtp_conta ADD CONSTRAINT t_mtp_conta_num_conta_un UNIQUE ( num_conta );

CREATE TABLE t_mtp_criptoativo (
    idt_criptoativo       NUMBER(3) NOT NULL,
    idt_carteira          NUMBER(5) NOT NULL,
    sig_criptoativo       CHAR(3 CHAR) NOT NULL,
    nom_criptoativo       VARCHAR2(30) NOT NULL,
    val_saldo_criptoativo NUMBER(10, 2) NOT NULL
);

ALTER TABLE t_mtp_criptoativo ADD CONSTRAINT t_mtp_criptoativo_pk PRIMARY KEY ( idt_criptoativo );

CREATE TABLE t_mtp_suporte_cliente (
    num_ticket       NUMBER(5) NOT NULL,
    num_cpf          NUMBER(11) NOT NULL,
    tipo_solicitacao VARCHAR2(50 CHAR) NOT NULL,
    des_solicitacao  VARCHAR2(200 CHAR) NOT NULL,
    dat_abertura     DATE NOT NULL,
    dat_fechamento   DATE
);

ALTER TABLE t_mtp_suporte_cliente ADD CONSTRAINT t_mtp_suporte_cliente_pk PRIMARY KEY ( num_ticket );

CREATE TABLE t_mtp_transacao (
    idt_transacao           NUMBER(5) NOT NULL,
    idt_carteira            NUMBER(5) NOT NULL,
    qtd_unidade_criptoativo NUMBER(10, 2) NOT NULL,
    dat_transacao           DATE NOT NULL,
    preco_momento           NUMBER(10, 2) NOT NULL
);

ALTER TABLE t_mtp_transacao ADD CONSTRAINT t_mtp_transacao_pk PRIMARY KEY ( idt_transacao );

CREATE TABLE t_mtp_transferencia (
    cod_transferencia NUMBER(3) NOT NULL,
    idt_transacao     NUMBER(5) NOT NULL,
    num_conta_destino NUMBER(5) NOT NULL
);

ALTER TABLE t_mtp_transferencia ADD CONSTRAINT t_mtp_transferencia_pk PRIMARY KEY ( cod_transferencia );

CREATE TABLE t_mtp_usuario (
    num_cpf          NUMBER(11) NOT NULL,
    des_nome         VARCHAR2(60 CHAR) NOT NULL,
    des_email        VARCHAR2(40) NOT NULL
);

ALTER TABLE t_mtp_usuario ADD CONSTRAINT t_mtp_usuario_pk PRIMARY KEY ( num_cpf );

ALTER TABLE t_mtp_usuario ADD CONSTRAINT t_mtp_usuario_num_cpf_un UNIQUE ( num_cpf );

CREATE TABLE t_mtp_venda (
    cod_venda     NUMBER(3) NOT NULL,
    idt_transacao NUMBER(5) NOT NULL
);

ALTER TABLE t_mtp_venda ADD CONSTRAINT t_mtp_venda_pk PRIMARY KEY ( cod_venda );

ALTER TABLE t_mtp_carteira
    ADD CONSTRAINT t_mtp_carteira_conta_fk FOREIGN KEY ( num_conta )
        REFERENCES t_mtp_conta ( num_conta );

ALTER TABLE t_mtp_compra
    ADD CONSTRAINT t_mtp_compra_transacao_fk FOREIGN KEY ( idt_transacao )
        REFERENCES t_mtp_transacao ( idt_transacao );

ALTER TABLE t_mtp_conta
    ADD CONSTRAINT t_mtp_conta_autenticador_fk FOREIGN KEY ( idt_autenticador1 )
        REFERENCES t_mtp_autenticador ( idt_autenticador );
 
ALTER TABLE t_mtp_criptoativo
    ADD CONSTRAINT t_mtp_criptoativo_t_mtp_carteira_fk FOREIGN KEY ( idt_carteira )
        REFERENCES t_mtp_carteira ( idt_carteira );

ALTER TABLE t_mtp_suporte_cliente
    ADD CONSTRAINT t_mtp_suporte_cliente_usuario_fk FOREIGN KEY ( num_cpf )
        REFERENCES t_mtp_usuario ( num_cpf )
            ON DELETE CASCADE;

ALTER TABLE t_mtp_transacao
    ADD CONSTRAINT t_mtp_transacao_carteira_fkv2 FOREIGN KEY ( idt_carteira )
        REFERENCES t_mtp_carteira ( idt_carteira );

ALTER TABLE t_mtp_transferencia
    ADD CONSTRAINT t_mtp_transfer_transacao_fk FOREIGN KEY ( idt_transacao )
        REFERENCES t_mtp_transacao ( idt_transacao );

ALTER TABLE t_mtp_usuario
    ADD CONSTRAINT t_mtp_usuario_autenticador_fk FOREIGN KEY ( idt_autenticador )
        REFERENCES t_mtp_autenticador ( idt_autenticador )
            ON DELETE CASCADE;

ALTER TABLE t_mtp_venda
    ADD CONSTRAINT t_mtp_venda_transacao_fk FOREIGN KEY ( idt_transacao )
        REFERENCES t_mtp_transacao ( idt_transacao );

CREATE SEQUENCE t_mtp_autenticador_idt_autenti START WITH 1 NOCACHE ORDER;

CREATE OR REPLACE TRIGGER t_mtp_autenticador_idt_autenti BEFORE
    INSERT ON t_mtp_autenticador
    FOR EACH ROW
    WHEN ( new.idt_autenticador IS NULL )
BEGIN
    :new.idt_autenticador := t_mtp_autenticador_idt_autenti.nextval;
END;
/



-- Relat rio do Resumo do Oracle SQL Developer Data Modeler: 
-- 
-- CREATE TABLE                            10
-- CREATE INDEX                             0
-- ALTER TABLE                             21
-- CREATE VIEW                              0
-- ALTER VIEW                               0
-- CREATE PACKAGE                           0
-- CREATE PACKAGE BODY                      0
-- CREATE PROCEDURE                         0
-- CREATE FUNCTION                          0
-- CREATE TRIGGER                           1
-- ALTER TRIGGER                            0
-- CREATE COLLECTION TYPE                   0
-- CREATE STRUCTURED TYPE                   0
-- CREATE STRUCTURED TYPE BODY              0
-- CREATE CLUSTER                           0
-- CREATE CONTEXT                           0
-- CREATE DATABASE                          0
-- CREATE DIMENSION                         0
-- CREATE DIRECTORY                         0
-- CREATE DISK GROUP                        0
-- CREATE ROLE                              0
-- CREATE ROLLBACK SEGMENT                  0
-- CREATE SEQUENCE                          1
-- CREATE MATERIALIZED VIEW                 0
-- CREATE MATERIALIZED VIEW LOG             0
-- CREATE SYNONYM                           0
-- CREATE TABLESPACE                        0
-- CREATE USER                              0
-- 
-- DROP TABLESPACE                          0
-- DROP DATABASE                            0
-- 
-- REDACTION POLICY                         0
-- 
-- ORDS DROP SCHEMA                         0
-- ORDS ENABLE SCHEMA                       0
-- ORDS ENABLE OBJECT                       0