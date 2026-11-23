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
-- Seed Data — Usuários de Demonstração (login manual)
-- Cidadão, Técnico Sabesp e Prefeitura para testes
-- ============================================================
INSERT INTO usuario (nome, email, senha, telefone, tipo_usuario, data_cadastro)
VALUES ('Carlos Eduardo Silva', 'carlos.silva@email.com', '123456', '(11) 98765-4321', 'COMUM', CURRENT_TIMESTAMP);

INSERT INTO usuario (nome, email, senha, telefone, tipo_usuario, data_cadastro)
VALUES ('Eng. Marina Sabesp', 'marina.santos@sabesp.com.br', 'admin123', '(11) 97777-1111', 'SABESP', CURRENT_TIMESTAMP);

INSERT INTO usuario (nome, email, senha, telefone, tipo_usuario, data_cadastro)
VALUES ('Fiscalização Prefeitura SP', 'fiscalizacao@prefeitura.sp.gov.br', 'pref123', '(11) 3113-8000', 'PREFEITURA', CURRENT_TIMESTAMP);

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
