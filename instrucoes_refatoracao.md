# Instruções para Reestruturação Segura do Projeto CorretoraCriptoativo

Este documento guia você na reestruturação do projeto, focando em limpar arquivos desnecessários e configurar o Git corretamente, **sem quebrar a funcionalidade** e respeitando as `diretrizes_desenvolvimento.md`.

**Objetivo:** Simplificar a estrutura de arquivos, remover arquivos não essenciais ou gerados automaticamente do controle de versão (Git) e garantir que o projeto continue compilando e funcionando.

**Pré-requisito:** Reverter o commit "Codebase Structure Refactor" que causou os problemas de compilação, voltando para um estado funcional do código.

## Passos para a Reestruturação Segura

1.  **Verificar Estrutura de Pacotes Essencial:**
    *   Confirme que a estrutura de pacotes principal (`src/main/java/org/example/`) contém os diretórios definidos nas diretrizes: `model`, `dao`, `ui`, `util` (e `factory`, `service` se aplicável e justificado).
    *   **NÃO APAGUE CÓDIGO FONTE ESSENCIAL (`.java`)!** Verifique se classes como `Usuario.java`, `UsuarioDAO.java`, `Conta.java`, `ContaDAO.java`, `Main.java`, `ConnectionFactory.java`, etc., ainda existem e estão nos pacotes corretos. Arquivos `.java` só devem ser removidos se você tiver **certeza absoluta** de que não são mais usados e sua funcionalidade não é necessária ou foi movida para outro lugar de forma correta.

2.  **Configurar/Atualizar o `.gitignore`:**
    *   Este é o passo **fundamental** para evitar que arquivos indesejados sejam enviados ao repositório.
    *   Crie ou edite o arquivo `.gitignore` na raiz do projeto (`MTP_Fase5/CorretoraCriptoativo_fase5/.gitignore`).
    *   Adicione as seguintes entradas (ou garanta que existam):

    ```gitignore
    # Arquivos compilados do Java
    *.class

    # Diretório de saída do Maven
    /target/

    # Arquivos e diretórios de configuração de IDEs comuns
    .idea/
    *.iml
    *.ipr
    *.iws
    .project
    .classpath
    .settings/

    # Arquivos de log
    *.log

    # Arquivos do sistema operacional
    .DS_Store
    Thumbs.db
    ```

3.  **Remover Arquivos Indesejados do Rastreamento do Git (se já foram adicionados):**
    *   Se arquivos como `.class` ou `.iml` foram acidentalmente adicionados e commitados no passado (como sugerido pela sua tentativa anterior de removê-los), apenas adicionar ao `.gitignore` não os remove do histórico ou do rastreamento atual.
    *   Use o comando `git rm --cached <arquivo>` para parar de rastrear esses arquivos sem excluí-los do seu disco local. Execute isso para os tipos de arquivo listados no `.gitignore`.
    *   **Exemplos (execute na raiz do projeto Maven - `MTP_Fase5/CorretoraCriptoativo_fase5/`):**
        *   `git rm --cached -r target/` (remove o diretório target do rastreamento)
        *   `git rm --cached **/*.class` (remove todos os arquivos .class)
        *   `git rm --cached *.iml` (remove arquivos .iml na raiz, ajuste o caminho se necessário)
        *   `git rm --cached -r .idea/` (se existir e tiver sido adicionado)
    *   Após executar esses comandos, faça um novo commit para registrar a remoção desses arquivos do rastreamento: `git commit -m "Fix: Remover arquivos compilados e de IDE do rastreamento Git"`

4.  **Limpar Arquivos Fonte Realmente Órfãos (Com Cuidado):**
    *   Após configurar o `.gitignore` e limpar o rastreamento, revise os arquivos `.java`.
    *   Existe algum arquivo `.java` que você sabe que não é mais usado por nenhuma outra parte do código? Alguma classe de teste antiga ou uma versão preliminar de uma funcionalidade que foi substituída?
    *   Se sim, e somente se tiver certeza, você pode removê-los. **Na dúvida, não remova.**

5.  **Verificar Aderência ao Padrão DAO:**
    *   Como a refatoração anterior quebrou isso, revise rapidamente a camada `ui` (especialmente `Main.java`).
    *   Garanta que **toda interação com o banco de dados** passe pelas classes DAO correspondentes (`UsuarioDAO`, `ContaDAO`, etc.).
    *   A UI **NÃO DEVE** chamar métodos como `salvarNoBanco`, `buscarPorCpf`, `listarTodos` diretamente em objetos do modelo (`Usuario`, `Conta`).
    *   **Exemplo Correto (em `Main.java` ou outra classe UI):**
        ```java
        // ... dentro de um método na UI ...
        try {
            UsuarioDAO usuarioDao = new UsuarioDAO(); // Instancia o DAO
            Usuario usuario = usuarioDao.buscarPorCpf(cpfDigitado); // Usa o DAO para buscar
            if (usuario != null) {
                System.out.println("Usuário encontrado: " + usuario.getNome());
            } else {
                System.out.println("Usuário não encontrado.");
            }
            usuarioDao.close(); // Fecha a conexão do DAO (se aplicável)
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
            // Tratar o erro apropriadamente
        }
        ```

6.  **Testar Compilação e Execução:**
    *   Após as mudanças, execute `mvn clean compile exec:java` (no diretório `MTP_Fase5/CorretoraCriptoativo_fase5/`) para garantir que tudo ainda compila e a aplicação inicia corretamente.
    *   Faça testes manuais básicos das funcionalidades principais (cadastro, login, consulta, etc.) para confirmar que nada foi quebrado.

Seguindo estes passos, você poderá limpar seu projeto e configurar o Git de forma adequada, mantendo o código funcional e alinhado às diretrizes. 