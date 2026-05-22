# POO_CRUD

Aplicação Java de console para cadastro, listagem, atualização e exclusão de clientes bancários, com criação automática de conta bancária vinculada ao cliente, consulta de endereço pelo ViaCEP e persistência em banco de dados MySQL.

## A finalidade

O objetivo da aplicação é demonstrar um CRUD usando Programação Orientada a Objetos em Java, separando responsabilidades entre classes de modelo, acesso a dados, configuração de banco e serviço externo.

A aplicação permite:

- Cadastrar clientes.
- Validar CPF e CEP antes do cadastro.
- Consultar automaticamente rua e cidade do cliente pela API ViaCEP.
- Criar automaticamente uma conta bancária para cada cliente.
- Listar clientes com suas respectivas contas.
- Atualizar o nome de um cliente.
- Excluir clientes e suas contas vinculadas.
- Consultar cotações de ações da B3 usando a API Alpha Vantage, quando a chave estiver configurada.

## A estrutura

```text
POO_CRUD/
├── src/
│   └── main/
│       └── java/
│           └── br/
│               └── com/
│                   └── POO_CRUD/
│                       ├── Main.java
│                       ├── config/
│                       │   └── DatabaseConnection.java
│                       ├── dao/
│                       │   ├── ClienteDAO.java
│                       │   └── ContaBancariaDAO.java
│                       ├── model/
│                       │   ├── Cliente.java
│                       │   └── ContaBancaria.java
│                       └── service/
│                           ├── AlphaVantageService.java
│                           └── ViaCepService.java
├── .env.example
├── .gitignore
├── pom.xml
└── README.md
```

Principais diretórios:

- `config`: configuração e inicialização da conexão com o MySQL.
- `dao`: classes responsáveis pelas operações SQL.
- `model`: classes que representam as entidades do sistema.
- `service`: integração com serviço externo de cotações.
- `Main.java`: ponto de entrada da aplicação e menu do console.

## Os requisitos

Para executar o projeto, é necessário ter instalado:

- Java JDK 17 ou superior.
- Maven.
- MySQL Server em execução localmente.
- IntelliJ IDEA, caso queira rodar pela IDE.
- Acesso à internet para consulta do ViaCEP durante o cadastro.
- Chave da API Alpha Vantage, opcional, para exibir cotações de ações.

Dependências usadas no `pom.xml`:

- `mysql-connector-j`: driver JDBC do MySQL.
- `dotenv-java`: leitura do arquivo `.env`.
- `gson`: leitura e conversão de JSON retornado pelas APIs ViaCEP e Alpha Vantage.

## As configurações de ambiente

O projeto usa um arquivo `.env` na raiz para configurar credenciais e variáveis sensíveis.

Crie um arquivo `.env` com base no arquivo `.env.example`:

```env
DB_NAME=banco_curso
DB_USER=seu_usuario_mysql
DB_PASSWORD=sua_senha_mysql
API_KEY_ALPHA=sua_chave_alpha_vantage
```

Substitua `sua_chave_alpha_vantage` pela sua chave real apenas no arquivo `.env` local. Não coloque chaves reais no `README.md`, no `.env.example` ou em qualquer arquivo versionado.

Descrição das variáveis:

- `DB_NAME`: nome do banco de dados MySQL. Se não for informado, a aplicação usa `banco_curso`.
- `DB_USER`: usuário do MySQL.
- `DB_PASSWORD`: senha do usuário do MySQL.
- `API_KEY_ALPHA`: chave da API Alpha Vantage. Essa variável é opcional; sem ela, a consulta de cotações será ignorada.

A API ViaCEP não precisa de chave de API e não exige configuração no `.env`.

## Os dados do MySQL

A aplicação se conecta ao MySQL em:

```text
jdbc:mysql://localhost:3306
```

O banco é criado automaticamente, caso ainda não exista:

```sql
CREATE DATABASE IF NOT EXISTS banco_curso
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

As tabelas também são criadas automaticamente na primeira conexão.

Tabela `cliente`:

```sql
CREATE TABLE IF NOT EXISTS cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    cep VARCHAR(8) NOT NULL,
    rua VARCHAR(150) NOT NULL,
    cidade VARCHAR(100) NOT NULL
);
```

Tabela `conta_bancaria`:

```sql
CREATE TABLE IF NOT EXISTS conta_bancaria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_conta VARCHAR(20) NOT NULL UNIQUE,
    saldo DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    cliente_id INT NOT NULL,
    CONSTRAINT fk_conta_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id)
        ON DELETE CASCADE
);
```

O relacionamento entre as tabelas é feito por `conta_bancaria.cliente_id`, que referencia `cliente.id`.

Ao excluir um cliente, a conta bancária vinculada também é excluída por causa da regra `ON DELETE CASCADE`.

Se o banco já existir sem os campos `rua` e `cidade`, a aplicação adiciona essas colunas automaticamente na inicialização.

## Como rodar pelo terminal

Na raiz do projeto, compile a aplicação:

```bash
mvn clean compile
```

Depois execute a classe principal:

```bash
mvn exec:java -Dexec.mainClass="br.com.POO_CRUD.Main"
```

Antes de rodar, confirme que:

- O MySQL Server está em execução.
- O arquivo `.env` existe na raiz do projeto.
- `DB_USER` e `DB_PASSWORD` estão corretos.
- O usuário informado tem permissão para criar banco e tabelas.
- O computador tem acesso à internet para consultar o ViaCEP durante o cadastro.

## Rodando no IntelliJ

1. Abra o projeto no IntelliJ IDEA.
2. Aguarde o Maven carregar as dependências do `pom.xml`.
3. Verifique se o SDK do projeto está configurado para Java 17 ou superior.
4. Crie o arquivo `.env` na raiz do projeto, usando o `.env.example` como referência.
5. Abra a classe `src/main/java/br/com/POO_CRUD/Main.java`.
6. Clique no botão de execução ao lado do método `main`.

Se a aplicação não encontrar o arquivo `.env`, verifique se o diretório de trabalho da configuração de execução está apontando para a raiz do projeto.

## Fluxo do programa

Ao iniciar, o programa exibe o menu:

```text
=== BANCO CONSOLE ===
1. Cadastrar Novo Cliente
2. Listar Clientes
3. Atualizar Cliente
4. Deletar Cliente
5. Sair
```

Fluxo das opções:

- `1. Cadastrar Novo Cliente`: solicita nome, CPF e CEP. O CPF deve ter 11 números e não pode estar cadastrado. O CEP deve ter 8 números. O sistema consulta o ViaCEP, retorna rua e cidade, salva esses dados e cria uma conta bancária com saldo inicial `0.00`.
- `2. Listar Clientes`: mostra clientes cadastrados junto com CPF, CEP, rua, cidade, número da conta e saldo.
- `3. Atualizar Cliente`: solicita o ID do cliente e altera o nome.
- `4. Deletar Cliente`: solicita o ID e uma confirmação. Se confirmado, remove o cliente e a conta vinculada.
- `5. Sair`: encerra o programa.

Durante o cadastro, se `API_KEY_ALPHA` estiver configurada, o sistema consulta as ações `PETR4.SAO`, `VALE3.SAO`, `ITUB4.SAO` e `BBAS3.SAO`.

Ao concluir um cadastro, o sistema exibe um resumo com ID, nome, CPF, CEP, rua, cidade, conta gerada e saldo inicial.

## Classes Principais

`Main.java`

Responsável pelo menu da aplicação, leitura de dados pelo terminal e chamada dos DAOs e serviços.

`DatabaseConnection.java`

Responsável por:

- Ler as variáveis do `.env`.
- Criar o banco de dados, se necessário.
- Criar as tabelas `cliente` e `conta_bancaria`.
- Atualizar a tabela `cliente` com os campos `rua` e `cidade`, se eles ainda não existirem.
- Fornecer conexões JDBC para os DAOs.

`ClienteDAO.java`

Responsável pelas operações de banco relacionadas a clientes:

- Verificar se CPF já está cadastrado.
- Salvar cliente.
- Listar clientes com conta.
- Atualizar nome.
- Deletar cliente.

`ContaBancariaDAO.java`

Responsável por salvar uma conta bancária vinculada ao cliente.

`Cliente.java`

Modelo que representa os dados de um cliente:

- `id`
- `nome`
- `cpf`
- `cep`
- `rua`
- `cidade`

`ContaBancaria.java`

Modelo que representa os dados de uma conta bancária:

- `id`
- `numeroConta`
- `saldo`
- `clienteId`

`AlphaVantageService.java`

Responsável por consultar a API Alpha Vantage e exibir cotações de ações da B3.

Para Obter a Chave da API basta seguir as instruções.

<img width="1245" height="940" alt="1" src="https://github.com/user-attachments/assets/bace44a4-3875-49cd-9722-19fe86e632d3" />
Clique no botao: OBTENHA CHAVE API GRATUITA

<img width="1245" height="940" alt="2" src="https://github.com/user-attachments/assets/1d614ae3-b06e-40bd-b3ab-98542ec1da24" />
Preencha os campos para obter a CHAVE DA API.
OBS: Os campos podem ser preenchidos com dados ficticios.

<img width="1257" height="947" alt="3" src="https://github.com/user-attachments/assets/3e61f11c-e7c6-4720-bd24-d4e3d9674d19" />
Cole a CHAVE no campo: API_KEY_ALPHA=sua_chave_alpha_vantage do arquivo .env


`ViaCepService.java`

Responsável por consultar a API ViaCEP usando o CEP informado no cadastro e retornar a rua e a cidade do cliente.

## Observações importantes relacionadas ao .env

- O arquivo `.env` contém dados sensíveis e não deve ser versionado.
- O `.env` já está incluído no `.gitignore`.
- O arquivo `.env.example` deve ser versionado, pois serve como modelo para configurar o ambiente.
- `DB_USER` e `DB_PASSWORD` são obrigatórios para conexão com o MySQL.
- `DB_NAME` é opcional, pois a aplicação usa `banco_curso` como valor padrão.
- `API_KEY_ALPHA` é opcional. Se não for configurada, a aplicação continua funcionando e apenas ignora a consulta de cotações.
- A API ViaCEP não usa variável de ambiente nem chave de API.
- Caso use variáveis de ambiente do sistema operacional em vez do arquivo `.env`, a aplicação também consegue carregá-las.
- Nunca coloque senhas reais, chaves de API ou dados privados no `.env.example`.
- A chave real da Alpha Vantage deve ficar somente no arquivo `.env` local ou nas variáveis de ambiente do sistema operacional.
