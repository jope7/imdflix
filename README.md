# IMDFlix

Catálogo de filmes, séries e documentários (projeto final de Linguagem de Programação II, IMD/UFRN).
É uma aplicação desktop em Java, com interface gráfica em JavaFX, inspirada em plataformas de streaming:
o usuário cria uma conta, navega pelo catálogo, avalia títulos, monta a sua lista e recebe recomendações.
Todos os dados ficam em arquivos JSON na pasta `dados/`.

**Funcionalidades**

- Criar conta e entrar com um username único (o nome de exibição pode se repetir).
- Navegar pelo catálogo, buscar por título e filtrar por gênero.
- Avaliar títulos com nota de 0 a 10 e alterar a nota depois; a nota média de cada título é recalculada.
- Montar a lista pessoal ("Minha lista").
- Receber recomendações por dois critérios, que podem ser trocados na própria tela.

## Requisitos

- JDK: 25.0.5
- JavaFX SDK: 25.0.5
- Gson: 2.11.0 (já incluído em `lib/gson-2.11.0.jar`)

**Execute sempre a partir da raiz do projeto**, porque os caminhos `dados/catalogo.json` e
`dados/usuarios.json` são relativos à pasta de execução.

## Como compilar e executar

Ajuste o caminho do JavaFX para a pasta `lib` do SDK na sua máquina.

### Windows (PowerShell)

```powershell
$JAVAFX = "C:\caminho\javafx-sdk-25.0.5\lib"
javac --module-path $JAVAFX --add-modules javafx.controls -cp "lib/gson-2.11.0.jar" -d out (Get-ChildItem src,test -Recurse -Filter *.java).FullName
java --module-path $JAVAFX --add-modules javafx.controls -cp "out;lib/gson-2.11.0.jar" ui.App
```

### Linux / macOS / WSL

```bash
JAVAFX=~/javafx-sdk-25.0.5/lib
javac --module-path $JAVAFX --add-modules javafx.controls -cp lib/gson-2.11.0.jar -d out $(find src test -name "*.java")
java --module-path $JAVAFX --add-modules javafx.controls -cp "out:lib/gson-2.11.0.jar" ui.App
```

A pasta `out` é gerada na compilação e não é versionada (está no `.gitignore`).

## Como usar

1. Ao abrir, entre com um usuário de exemplo (`76ope`, `trembo_dev` ou `vitinhozip`) ou crie uma conta.
   O username tem de 3 a 20 caracteres (letras minúsculas, números e `_`); o nome pode se repetir.
2. No catálogo, busque pelo título ou filtre por gênero e abra um título.
3. No detalhe, avalie com uma nota de 0 a 10 (aceita vírgula ou ponto) e/ou adicione o título à sua lista.
4. Em "Minha lista", veja e remova os títulos salvos.
5. Em "Recomendações", escolha o critério na caixa de seleção.

Exemplo para conferir as recomendações: com `trembo_dev` (que ainda não avaliou nada), o critério
"Mais bem avaliados" mostra Free Solo, Dark e Matrix, e "Pelo seu gosto" fica vazio. Depois de dar nota 9
ao Matrix, "Pelo seu gosto" passa a sugerir Interestelar, que é do mesmo gênero (Ficção).

O programa grava as alterações nos arquivos de `dados/`. Para voltar ao estado de exemplo, use
`git restore dados`.

## Como executar os testes

Os testes usam `assert` puro. Lembre da flag `-ea`: sem ela o Java ignora os `assert`.
Depois de compilar (comando acima), na raiz do projeto:

```powershell
foreach ($t in "TesteMidia","TesteUsuario","TesteServicos","TesteExcecoes","TesteMidiaFactory","TesteCatalogoJson","TesteUsuarioJson","TesteSessaoUsuario","TesteCarregadorDados","TesteRecomendacao") { java -ea -cp "out;lib/gson-2.11.0.jar" "test.$t" }
```

```bash
for t in TesteMidia TesteUsuario TesteServicos TesteExcecoes TesteMidiaFactory TesteCatalogoJson TesteUsuarioJson TesteSessaoUsuario TesteCarregadorDados TesteRecomendacao; do java -ea -cp "out:lib/gson-2.11.0.jar" test.$t; done
```

Cada teste termina com uma linha dizendo que passou; se um `assert` falhar, a mensagem indica qual caso quebrou.
Os testes de persistência usam arquivos temporários e nunca alteram os dados reais.

## Estrutura

- `src/modelo`: `Midia` (abstrata), `Filme`, `Serie`, `Documentario`, `Usuario` e as interfaces `Avaliavel` e `Exibivel`
- `src/servico`: `Catalogo`, `GerenciadorUsuarios`, `CarregadorDados`, `SessaoUsuario` e a recomendação (`Recomendador`, `RecomendacaoStrategy`, `RecomendacaoPorNota`, `RecomendacaoPorGenero`)
- `src/persistencia`: interfaces de repositório, repositórios em JSON e `MidiaFactory`
- `src/excecoes`: exceções próprias do sistema
- `src/ui`: telas em JavaFX
- `test`: testes unitários
- `dados`: arquivos JSON de exemplo
- `docs`: diagramas UML de classes e de sequência

## Conceitos de POO no código

| Conceito | Onde está |
|---|---|
| Classe abstrata com subclasses concretas | `Midia`, com `Filme`, `Serie` e `Documentario` |
| Interfaces implementadas por mais de uma classe | `Exibivel` (nas três subclasses), `Avaliavel` (em `Midia`) e `RecomendacaoStrategy` (em duas estratégias) |
| Polimorfismo | catálogo, avaliações, recomendações e telas tratam qualquer título como `Midia`; `getTipo()` e `getResumoExibicao()` mudam conforme a subclasse |
| Encapsulamento | atributos privados com acesso por getters; os construtores validam os dados |
| Tratamento de exceções | veja a seção abaixo |
| Testes unitários | pasta `test`, com `assert` puro |

## Padrões de projeto

### Factory (MidiaFactory)
O catálogo é gravado em JSON com um campo `tipo`. A `MidiaFactory` concentra em um único lugar a decisão de
qual subclasse de `Midia` criar (Filme, Serie ou Documentario) e a validação dos campos lidos. Assim, o
restante do sistema não precisa conhecer os detalhes de cada subclasse nem o formato do arquivo. Acrescentar um
novo tipo de mídia exige a classe nova, um caso na `MidiaFactory` e o trecho correspondente na gravação do
`CatalogoJsonRepositorio`.

### Singleton (SessaoUsuario)
Existe um único usuário logado por vez, e várias telas precisam consultá-lo. A `SessaoUsuario` garante uma
instância compartilhada, evitando passar o usuário de tela em tela. A instância é criada de forma preguiçosa e
segura para várias threads (idiom do "holder"). Os repositórios, ao contrário, são instâncias comuns que
recebem o caminho do arquivo, o que permite testá-los com arquivos temporários.

### Strategy (RecomendacaoStrategy)
Existem várias formas razoáveis de recomendar um título: pelas melhores notas de todos os usuários
(`RecomendacaoPorNota`) ou pelo gosto do próprio usuário (`RecomendacaoPorGenero`). Cada algoritmo é uma classe
que implementa `RecomendacaoStrategy`, e o `Recomendador` usa a estratégia escolhida sem conhecer os detalhes
dela. Assim a tela pode trocar o critério em tempo de execução, e criar um novo critério exige apenas uma
classe nova, sem alterar o `Recomendador` (basta registrar a nova estratégia na tela de recomendações).

## Princípios SOLID, coesão e acoplamento

- **Responsabilidade única (S):** cada classe tem um assunto. `Catalogo` cuida dos títulos, `GerenciadorUsuarios`
  do cadastro e da entrada, os repositórios só do armazenamento, a `MidiaFactory` só da criação e as telas só da
  apresentação (as regras ficam no modelo e nos serviços).
- **Aberto/fechado (O):** novos critérios de recomendação entram como novas classes de `RecomendacaoStrategy`,
  sem mudar o `Recomendador`.
- **Substituição de Liskov (L):** `Filme`, `Serie` e `Documentario` podem ser usadas onde uma `Midia` é esperada.
  `Catalogo`, `Usuario.avaliar`, as estratégias e as telas não verificam o tipo concreto. A única exceção é a
  gravação em JSON, que usa `instanceof` porque precisa conhecer os campos de cada subclasse.
- **Segregação de interfaces (I):** as interfaces são pequenas e específicas (`Avaliavel`, `Exibivel`,
  `RecomendacaoStrategy`), e há um repositório para o catálogo e outro para os usuários, em vez de uma interface única.
- **Inversão de dependência (D):** `Catalogo` e `GerenciadorUsuarios` dependem das interfaces
  `CatalogoRepositorio` e `UsuarioRepositorio`, e o `Recomendador` depende de `RecomendacaoStrategy`. Por isso os
  testes dos serviços usam repositórios em memória. Só o `CarregadorDados`, que monta o sistema, conhece as
  classes JSON concretas.
- **Coesão e acoplamento:** as classes do modelo não conhecem arquivos nem telas; os serviços conhecem o modelo e
  as interfaces de repositório; as telas só chamam os serviços. As dependências seguem um único sentido:
  interface gráfica, serviços, modelo.

## Tratamento de exceções

- `NotaInvalidaException`: nota fora do intervalo de 0 a 10.
- `TituloDuplicadoException`: título já existente no catálogo (sem diferenciar maiúsculas).
- `UsernameJaExisteException`: username já em uso (sem diferenciar maiúsculas).
- `DadosInvalidosException`: arquivo JSON malformado ou com dados inválidos. A mensagem indica o arquivo, a
  posição do item e o motivo. Ao abrir o programa, ela é mostrada em um alerta, e o sistema não segue com dados
  pela metade.
- `IllegalArgumentException`: valores inválidos na criação de objetos (título vazio, username fora do formato etc.).

## Regras dos dados

- O `username` é único (sem diferenciar maiúsculas); o nome de exibição pode se repetir.
- O título é único no catálogo (sem diferenciar maiúsculas).
- As notas ficam no arquivo de usuários; as médias das mídias são recalculadas ao carregar.
- Títulos que não existem mais no catálogo são ignorados ao carregar os usuários e somem do arquivo na próxima gravação.
- Arquivos JSON inválidos fazem o programa avisar o erro, sem carregar dados pela metade.
- Arquivo ausente ou em branco equivale a lista vazia; ele é criado na primeira gravação.

## Documentação

- [Diagramas de classes](docs/diagrama-classes.md): domínio, serviços, persistência e interface gráfica.
- [Diagramas de sequência](docs/diagrama-sequencia.md): avaliar um título, inicialização do sistema e recomendações.
- As imagens dos diagramas estão em `docs/img`.
- O código-fonte tem Javadoc nas classes principais.

## Limitações conhecidas

- Não há senha: a entrada é só pelo username.
- A gravação regrava o arquivo JSON inteiro, o que é suficiente para um uso local por uma pessoa de cada vez.
- A busca no catálogo é pelo título completo (sem diferenciar maiúsculas).

## Equipe

- JOACKYM PEDRO ARAUJO COELHO
- LUIZ CARLOS VICTOR PINTO
- VITOR GABRIEL DA COSTA SOUSA
