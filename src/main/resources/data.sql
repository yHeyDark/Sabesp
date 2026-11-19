-- ============================================================
-- Seed Data — StatusChamado
-- ============================================================
INSERT INTO status_chamado (descricao) VALUES ('Aberto');
INSERT INTO status_chamado (descricao) VALUES ('Em Andamento');
INSERT INTO status_chamado (descricao) VALUES ('Resolvido');
INSERT INTO status_chamado (descricao) VALUES ('Fechado');

-- ============================================================
-- Seed Data — TipoProblema
-- ============================================================
INSERT INTO tipo_problema (descricao) VALUES ('Vazamento de Esgoto');
INSERT INTO tipo_problema (descricao) VALUES ('Bueiro Entupido');
INSERT INTO tipo_problema (descricao) VALUES ('Mau Cheiro');
INSERT INTO tipo_problema (descricao) VALUES ('Transbordamento de Fossa');
INSERT INTO tipo_problema (descricao) VALUES ('Esgoto a Céu Aberto');
INSERT INTO tipo_problema (descricao) VALUES ('Refluxo de Esgoto');
INSERT INTO tipo_problema (descricao) VALUES ('Rede de Esgoto Danificada');
INSERT INTO tipo_problema (descricao) VALUES ('Outros');

-- ============================================================
-- Seed Data — Usuários Iniciais (Apenas Prefeitura para testes formais)
-- ============================================================
INSERT INTO usuario (nome, email, senha, telefone, tipo_usuario, data_cadastro)
VALUES ('Prefeitura Municipal de Santos - Fiscalização', 'fiscalizacao@santos.sp.gov.br', 'santos2026', '(13) 3218-4000', 'PREFEITURA', CURRENT_TIMESTAMP);

-- ============================================================
-- Seed Data — Endereços Iniciais
-- ============================================================
INSERT INTO endereco (logradouro, numero, complemento, bairro, cidade, estado, cep)
VALUES ('Avenida Paulista', '1578', 'Em frente ao MASP', 'Bela Vista', 'São Paulo', 'SP', '01310-200');

INSERT INTO endereco (logradouro, numero, complemento, bairro, cidade, estado, cep)
VALUES ('Rua Augusta', '450', 'Próximo à esquina', 'Consolação', 'São Paulo', 'SP', '01304-000');

INSERT INTO endereco (logradouro, numero, complemento, bairro, cidade, estado, cep)
VALUES ('Rua Domingos de Morais', '1200', 'Vila Mariana', 'Vila Mariana', 'São Paulo', 'SP', '04010-100');

-- ============================================================
-- Seed Data — Chamados de Exemplo (REMOVIDOS - Sistema inicia vazio)
-- ============================================================
-- Nenhum chamado pré-cadastrado. O cidadão cria seus próprios chamados.

-- ============================================================
-- Seed Data — Comentários (REMOVIDOS - Sistema inicia vazio)
-- ============================================================
-- Nenhum comentário pré-cadastrado.
