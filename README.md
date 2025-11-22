## PASSO A PASSO PARA IMPORTAR O PROJETO NO ECLIPSE

1. Assista ao tutorial (trecho recomendado): https://www.youtube.com/watch?v=HuhJ22oHyfc — veja de _2:30 a 5:00_.

2. Crie uma senha/token no GitHub:

   - Acesse `Settings` -> `Developer settings` (final da página) -> `Personal access tokens` -> `Tokens (classic)` -> `Generate new (classic)`.
   - Dê a permissão `repo` e gere o token. Copie o token e cole no campo de senha quando o Eclipse solicitar.

3. No Eclipse (Git Repositories):

   - `Clone a Git repository` -> `Clone URI` -> cole `https://github.com/R0bbyt3/2311293-2311833` na URI.
   - Usuário: seu username; Senha: o token gerado; ative o store se desejar.
   - Avance com `Next`/`OK` em todas as etapas. Se a pasta de destino já existir, delete-a antes de clonar (ou escolha outro diretório) e repita o passo para que o Eclipse processe corretamente.

4. Importar projeto:

   - `Import` -> `Projects from Git` -> `Existing local repository` -> selecione o repositório clonado (`2311293-2311833`) e importe os projetos.

5. Uso do Git no Eclipse:

   - O restante do vídeo mostra como usar push/pull e a view de `Git Staging`. Use a aba `Git Staging` para preparar commits, push e pull.

6. Problemas de versão do Java (se ocorrerem):
   - Baixe e instale o JDK (ex.: JDK 24).
   - Localize a pasta de instalação (ex.: `C:\Program Files\Java\jdk-24`).
   - No Eclipse: `Window` -> `Preferences` -> `Installed JREs` -> `Add` -> `Standard VM` -> aponte para a pasta do JDK -> `Finish` -> selecione o JDK novo -> `Apply and Close`.
   - Link de download (exemplo): https://download.oracle.com/java/24/archive/jdk-24.0.2_windows-x64_bin.exe
