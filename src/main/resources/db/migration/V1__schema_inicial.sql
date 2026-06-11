-- ============================================================
-- Biblioteca Cultura — Schema V1
-- Run this script once on your PostgreSQL database before
-- starting the application for the first time.
-- ============================================================

-- -------------------------
-- FUNCIONARIOS (staff/librarians)
-- RF014, RF015, RNF008
-- -------------------------
CREATE TABLE funcionarios (
    id          BIGSERIAL       PRIMARY KEY,
    nome        VARCHAR(150)    NOT NULL,
    cpf         VARCHAR(14)     NOT NULL UNIQUE,  -- ###.###.###-##
    matricula   VARCHAR(30)     NOT NULL UNIQUE,
    cargo       VARCHAR(30)     NOT NULL,         -- 'BIBLIOTECARIO' | 'ADMINISTRADOR'
    senha_hash  VARCHAR(255)    NOT NULL,
    ativo       BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- -------------------------
-- CATEGORIAS (book genres/categories)
-- RF002 — JComboBox source
-- -------------------------
CREATE TABLE categorias (
    id   BIGSERIAL    PRIMARY KEY,
    nome VARCHAR(80)  NOT NULL UNIQUE
);

-- -------------------------
-- LIVROS (books)
-- RF002-RF005
-- -------------------------
CREATE TABLE livros (
    id               BIGSERIAL    PRIMARY KEY,
    titulo           VARCHAR(255) NOT NULL,
    autor            VARCHAR(150) NOT NULL,
    ano_publicacao   SMALLINT     NOT NULL,
    categoria_id     BIGINT       NOT NULL REFERENCES categorias(id),
    criado_em        TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- -------------------------
-- EXEMPLARES (physical copies — one per book on first registration)
-- RF002 auto-creates one with status 'DISPONIVEL'
-- RF006/RF008 flip the status
-- -------------------------
CREATE TABLE exemplares (
    id        BIGSERIAL   PRIMARY KEY,
    livro_id  BIGINT      NOT NULL REFERENCES livros(id) ON DELETE CASCADE,
    status    VARCHAR(15) NOT NULL DEFAULT 'DISPONIVEL'
        CHECK (status IN ('DISPONIVEL', 'INDISPONIVEL'))
);

-- -------------------------
-- CLIENTES (library patrons)
-- RF010-RF013
-- -------------------------
CREATE TABLE clientes (
    id                  BIGSERIAL    PRIMARY KEY,
    nome                VARCHAR(150) NOT NULL,
    cpf                 VARCHAR(14)  NOT NULL UNIQUE,
    email               VARCHAR(120) NOT NULL,
    telefone            VARCHAR(20)  NOT NULL,
    status_situacional  VARCHAR(15)  NOT NULL DEFAULT 'SEM_MULTA'
        CHECK (status_situacional IN ('SEM_MULTA', 'COM_MULTA', 'BLOQUEADO')),
    saldo_multa         NUMERIC(8,2) NOT NULL DEFAULT 0.00,
    ativo               BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em           TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- -------------------------
-- EMPRESTIMOS (loans)
-- RF006-RF009
-- -------------------------
CREATE TABLE emprestimos (
    id                   BIGSERIAL   PRIMARY KEY,
    cliente_id           BIGINT      NOT NULL REFERENCES clientes(id),
    exemplar_id          BIGINT      NOT NULL REFERENCES exemplares(id),
    data_saida           DATE        NOT NULL DEFAULT CURRENT_DATE,
    data_previsao        DATE        NOT NULL,   -- data_saida + 14 days (RF006)
    data_devolucao_real  DATE,                   -- NULL while active; set on return
    multa_aplicada       NUMERIC(8,2) NOT NULL DEFAULT 0.00,
    funcionario_id       BIGINT      NOT NULL REFERENCES funcionarios(id),
    criado_em            TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES — keeps the 6-second response requirement (RNF005)
-- ============================================================
CREATE INDEX idx_livros_titulo      ON livros(titulo);
CREATE INDEX idx_livros_autor       ON livros(autor);
CREATE INDEX idx_livros_categoria   ON livros(categoria_id);
CREATE INDEX idx_clientes_cpf       ON clientes(cpf);
CREATE INDEX idx_clientes_nome      ON clientes(nome);
CREATE INDEX idx_emprestimos_cliente ON emprestimos(cliente_id);
CREATE INDEX idx_emprestimos_exemplar ON emprestimos(exemplar_id);
CREATE INDEX idx_emprestimos_ativos  ON emprestimos(exemplar_id)
    WHERE data_devolucao_real IS NULL;

-- ============================================================
-- SEED DATA — default admin + sample categories
-- Password is 'admin123' — CHANGE THIS in production.
-- The hash below is BCrypt of 'admin123'; swap for a proper hash.
-- ============================================================
INSERT INTO funcionarios (nome, cpf, matricula, cargo, senha_hash)
VALUES ('Administrador', '000.000.000-00', 'ADM-001', 'ADMINISTRADOR',
        '$2a$12$placeholder_replace_with_real_bcrypt_hash');

INSERT INTO categorias (nome) VALUES
    ('Ficção Científica'),
    ('Romance'),
    ('História'),
    ('Tecnologia'),
    ('Didático'),
    ('Literatura Brasileira'),
    ('Poesia'),
    ('Biografia'),
    ('Filosofia'),
    ('Direito');
