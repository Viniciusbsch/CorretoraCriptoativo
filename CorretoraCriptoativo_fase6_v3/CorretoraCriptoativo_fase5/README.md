# Corretora Criptoativo - Fase 5

Este projeto implementa uma aplicação Java para uma corretora de criptoativos, com integração ao banco de dados Oracle.

## Estrutura do Projeto

O projeto foi configurado usando Maven para gerenciamento de dependências e build. A estrutura segue o padrão Maven:

```
├── pom.xml                      # Arquivo de configuração do Maven
├── src
│   ├── main
│   │   └── java
│   │       └── org
│   │           └── example
│   │               ├── App.java # Classe principal
│   │               ├── classes  # Classes do domínio
│   │               └── factory
│   │                   └── ConnectionFactory.java # Fábrica de conexões
│   └── test
│       └── java                 # Testes unitários
```

## Tecnologias Utilizadas

- Java
- Maven (gerenciamento de dependências e build)
- Oracle Database (JDBC)

## Razões para Utilizar o Maven

1. **Gerenciamento de Dependências**: O Maven simplifica o gerenciamento de bibliotecas externas, como o driver JDBC do Oracle, eliminando a necessidade de gerenciar JARs manualmente.

2. **Estrutura Padronizada**: O Maven impõe uma estrutura de diretórios consistente, facilitando a organização e manutenção do código.

3. **Ciclo de Vida de Build**: Fornece fases padronizadas para compilar, testar, empacotar e distribuir o projeto.

4. **Portabilidade**: Facilita a execução do projeto em diferentes ambientes e por diferentes desenvolvedores.

5. **Plugins**: Permite estender a funcionalidade através de plugins para tarefas como empacotamento, geração de relatórios, etc.

## Como Executar o Projeto

### Pré-requisitos

- JDK 17 ou superior
- Maven 3.6 ou superior
- Acesso ao banco de dados Oracle

### Passo a Passo para Execução

1. **Clone o repositório:**
   ```bash
   git clone [URL_DO_REPOSITORIO]
   cd CorretoraCriptoativo_fase5
   ```

2. **Verifique a configuração do banco de dados:**
   Abra o arquivo `src/main/java/org/example/factory/ConnectionFactory.java` e certifique-se de que as credenciais de conexão estão corretas

3. **Execute os scripts SQL:**
   Antes de executar a aplicação, certifique-se de que as tabelas necessárias foram criadas no banco de dados. Os scripts SQL estão disponíveis na pasta `SCRIPT-DDL`:
   - `CREATE.ddl`: Cria as tabelas necessárias
   - `INSERT.sql`: Insere dados iniciais (opcional)

4. **Compile e execute o projeto:**
   ```bash
   mvn clean compile exec:java
   ```

5. **Ou, alternativamente, gere um JAR e execute-o:**
   ```bash
   mvn package
   java -jar target/CorretoraCriptoativo_fase5-1.0-SNAPSHOT.jar
   ```

### Funcionalidades Disponíveis

Após executar a aplicação, você terá acesso às seguintes funcionalidades:

1. **Menu de Acesso:**
   - Fazer Login
   - Criar Novo Usuário
   - Listar Usuários Cadastrados
   - Menu Banco de Dados

2. **Menu Banco de Dados:**
   - Inserir novo usuário
   - Buscar usuário por CPF
   - Listar todos os usuários
   - Atualizar usuário
   - Excluir usuário

3. **Menu Principal (após login):**
   - Criar nova conta
   - Acessar conta existente
   - Ver reservas da corretora
   - Ver minhas contas
   - Suporte ao cliente

### Solução de Problemas

- **Erro de conexão com o banco de dados:** Verifique se as credenciais em `ConnectionFactory.java` estão corretas e se o banco de dados está acessível.
- **Erro "Table or view does not exist":** Execute o script `CREATE.ddl` para criar as tabelas necessárias.
- **Erro de compilação:** Certifique-se de que o JDK 17 está instalado e configurado corretamente.

## Configuração do Banco de Dados

A conexão com o banco de dados Oracle é gerenciada pela classe `ConnectionFactory`. 
A configuração da conexão (URL, usuário e senha) está definida nessa classe. 