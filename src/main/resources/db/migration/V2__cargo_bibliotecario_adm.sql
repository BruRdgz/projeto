-- ============================================================
-- Biblioteca Cultura — Migration V2
-- Adiciona o cargo BIBLIOTECARIO_ADM e corrige o seed inicial.
-- ============================================================

-- 1. Amplia o CHECK da coluna cargo para aceitar BIBLIOTECARIO_ADM
--    (remove a constraint antiga e cria nova)
ALTER TABLE funcionarios
DROP CONSTRAINT IF EXISTS funcionarios_cargo_check;

ALTER TABLE funcionarios
    ADD CONSTRAINT funcionarios_cargo_check
        CHECK (cargo IN ('BIBLIOTECARIO', 'BIBLIOTECARIO_ADM'));

-- 2. Renomeia o registro 'ADMINISTRADOR' do V1 (se existir) para o novo enum
UPDATE funcionarios
SET cargo = 'BIBLIOTECARIO_ADM'
WHERE cargo = 'ADMINISTRADOR';

-- 3. Garante que exista pelo menos um BIBLIOTECARIO_ADM com senha conhecida.
--    Matrícula: ADM-001  |  Senha: admin123 (altere em produção!)
--    A senha abaixo é texto simples pois o projeto ainda não usa BCrypt.
--    Quando Spring Security for adicionado, substitua por um hash BCrypt real.
INSERT INTO funcionarios (nome, cpf, matricula, cargo, senha_hash, ativo)
VALUES ('Administrador Geral', '000.000.000-00', 'ADM-001', 'BIBLIOTECARIO_ADM', 'admin123', TRUE)
    ON CONFLICT (matricula) DO UPDATE
                                   SET cargo      = 'BIBLIOTECARIO_ADM',
                                   senha_hash = EXCLUDED.senha_hash;

-- 4. Cria índice de suporte à pesquisa por cargo (usado em FuncionarioRepository#search)
CREATE INDEX IF NOT EXISTS idx_funcionarios_cargo ON funcionarios(cargo);