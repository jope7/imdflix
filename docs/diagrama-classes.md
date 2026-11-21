# Diagramas de classes: IMDFlix

Os diagramas foram montados a partir do código do repositório e estão divididos por assunto, para ficarem legíveis. O GitHub renderiza os blocos `mermaid` automaticamente. Para gerar imagens (PNG ou SVG) para os slides, cole o código em https://mermaid.live.

Legenda: `+` público, `-` privado, `*` método abstrato, `$` método estático, `<|--` herança, `<|..` implementação de interface, `..>` dependência (usa ou cria), `-->` associação, `o--` agregação.

## 1. Domínio: mídias, usuário e interfaces

Mostra a classe abstrata `Midia` com suas três subclasses concretas, as duas interfaces (`Avaliavel` e `Exibivel`) e o usuário. O polimorfismo acontece quando o sistema trata qualquer título como `Midia` (avaliar, mostrar o resumo de exibição) sem saber se é filme, série ou documentário.

```mermaid
classDiagram
    direction TB

    class Avaliavel {
        <<interface>>
        +avaliar(double nota) void
        +removerAvaliacao(double nota) void
        +substituirAvaliacao(double notaAntiga, double notaNova) void
        +getNotaMedia() double
        +getQuantidadeAvaliacoes() int
    }

    class Exibivel {
        <<interface>>
        +getDuracaoTotalMinutos() int
        +getResumoExibicao() String
    }

    class Midia {
        <<abstract>>
        -String titulo
        -String genero
        -int anoLancamento
        -double somaNotas
        -int quantidadeNotas
        +getTipo()* String
        +validarNota(double nota)$ void
        +getTitulo() String
        +getGenero() String
        +getAnoLancamento() int
    }

    class Filme {
        -int duracaoMinutos
        -String diretor
        +getTipo() String
    }

    class Serie {
        -int temporadas
        -int episodiosPorTemporada
        -int duracaoMediaEpisodio
        +getTipo() String
    }

    class Documentario {
        -int duracaoMinutos
        -String tema
        +getTipo() String
    }

    class Usuario {
        -String username
        -String nome
        -List~String~ minhaLista
        -Map~String, Double~ avaliacoes
        +restaurar(username, nome, minhaLista, avaliacoes)$ Usuario
        +normalizarUsername(String username)$ String
        +adicionarNaLista(Midia midia) void
        +removerDaLista(Midia midia) void
        +avaliar(Midia midia, double nota) void
        +getUsername() String
        +getNome() String
        +setNome(String nome) void
        +getMinhaLista() List~String~
        +getAvaliacoes() Map~String, Double~
    }

    class NotaInvalidaException {
        <<exception>>
    }

    Avaliavel <|.. Midia
    Midia <|-- Filme
    Midia <|-- Serie
    Midia <|-- Documentario
    Exibivel <|.. Filme
    Exibivel <|.. Serie
    Exibivel <|.. Documentario

    Usuario ..> Midia : avalia
    Midia ..> NotaInvalidaException : lança
    Usuario ..> NotaInvalidaException : propaga
```

**Encapsulamento:** todos os atributos são privados, com acesso por getters. O `Usuario` guarda os títulos (texto), não os objetos `Midia`, o que o mantém independente do catálogo carregado.

## 2. Serviços: catálogo, usuários, sessão e recomendação

Mostra onde ficam as regras do sistema e dois padrões de projeto: **Singleton** (`SessaoUsuario`) e **Strategy** (`RecomendacaoStrategy` com o contexto `Recomendador`).

```mermaid
classDiagram
    direction TB

    class Catalogo {
        -CatalogoRepositorio repositorio
        -Map~String, Midia~ midias
        +cadastrar(Midia midia) void
        +buscarPorTitulo(String titulo) Optional~Midia~
        +filtrarPorGenero(String genero) List~Midia~
        +listarTodos() List~Midia~
        +aplicarAvaliacoes(Collection~Usuario~ usuarios) void
    }

    class GerenciadorUsuarios {
        -UsuarioRepositorio repositorio
        -Map~String, Usuario~ usuarios
        +cadastrar(String username, String nome) Usuario
        +entrar(String username) Optional~Usuario~
        +salvar(Usuario usuario) void
        +listarTodos() List~Usuario~
    }

    class SessaoUsuario {
        <<Singleton>>
        -Usuario usuarioAtual
        -SessaoUsuario()
        +getInstancia()$ SessaoUsuario
        +entrar(Usuario usuario) void
        +sair() void
        +haUsuarioLogado() boolean
        +getUsuarioAtual() Optional~Usuario~
    }

    class Recomendador {
        -RecomendacaoStrategy estrategia
        +setEstrategia(RecomendacaoStrategy estrategia) void
        +getEstrategia() RecomendacaoStrategy
        +recomendar(Usuario usuario, Catalogo catalogo, int limite) List~Midia~
    }

    class RecomendacaoStrategy {
        <<interface>>
        +getNome() String
        +getDescricao() String
        +recomendar(Usuario usuario, List catalogo, int limite) List~Midia~
    }

    class RecomendacaoPorNota {
        +getNome() String
        +getDescricao() String
        +recomendar(Usuario usuario, List catalogo, int limite) List~Midia~
    }

    class RecomendacaoPorGenero {
        +double NOTA_MINIMA_GOSTO
        +getNome() String
        +getDescricao() String
        +recomendar(Usuario usuario, List catalogo, int limite) List~Midia~
    }

    class CatalogoRepositorio {
        <<interface>>
    }
    class UsuarioRepositorio {
        <<interface>>
    }
    class Midia {
        <<abstract>>
    }
    class Usuario
    class TituloDuplicadoException {
        <<exception>>
    }
    class UsernameJaExisteException {
        <<exception>>
    }

    Catalogo --> CatalogoRepositorio : persiste via
    Catalogo "1" o-- "*" Midia : contém
    GerenciadorUsuarios --> UsuarioRepositorio : persiste via
    GerenciadorUsuarios "1" o-- "*" Usuario : contém
    SessaoUsuario --> "0..1" Usuario : usuarioAtual

    Recomendador --> RecomendacaoStrategy : estrategia
    Recomendador ..> Catalogo : lê os títulos
    RecomendacaoStrategy <|.. RecomendacaoPorNota
    RecomendacaoStrategy <|.. RecomendacaoPorGenero
    RecomendacaoStrategy ..> Usuario : usa
    RecomendacaoStrategy ..> Midia : recomenda

    Catalogo ..> TituloDuplicadoException : lança
    GerenciadorUsuarios ..> UsernameJaExisteException : lança

    note for SessaoUsuario "Padrão Singleton: um único usuário logado, compartilhado por todas as telas"
    note for RecomendacaoStrategy "Padrão Strategy: cada critério de recomendação é um algoritmo intercambiável"
```

## 3. Persistência e carregamento dos dados

Mostra as interfaces de repositório (o restante do sistema depende delas, não do JSON), as implementações em JSON, o padrão **Factory** (`MidiaFactory`) e o `CarregadorDados`, que garante a ordem: primeiro o catálogo, depois os usuários.

```mermaid
classDiagram
    direction TB

    class CatalogoRepositorio {
        <<interface>>
        +salvar(Midia midia) void
        +listarTodos() List~Midia~
        +buscarPorTitulo(String titulo) Optional~Midia~
    }

    class UsuarioRepositorio {
        <<interface>>
        +salvar(Usuario usuario) void
        +listarTodos() List~Usuario~
        +buscarPorUsername(String username) Optional~Usuario~
    }

    class CatalogoJsonRepositorio {
        -Path caminho
        +salvar(Midia midia) void
        +listarTodos() List~Midia~
        +buscarPorTitulo(String titulo) Optional~Midia~
    }

    class UsuarioJsonRepositorio {
        -Path caminho
        -Map~String, Usuario~ usuarios
        +carregar(Path caminho, Function buscaMidia)$ UsuarioJsonRepositorio
        +salvar(Usuario usuario) void
        +listarTodos() List~Usuario~
        +buscarPorUsername(String username) Optional~Usuario~
    }

    class MidiaFactory {
        <<Factory>>
        +criar(JsonObject dados)$ Midia
    }

    class ArquivoJson {
        <<utilitário do pacote>>
        ~lerLista(Path caminho)$ JsonArray
        ~gravarLista(Path caminho, JsonArray lista)$ void
    }

    class CarregadorDados {
        -Path caminhoCatalogo
        -Path caminhoUsuarios
        +padrao()$ CarregadorDados
        +carregar() DadosCarregados
    }

    class DadosCarregados {
        <<record>>
        Catalogo catalogo
        CatalogoRepositorio catalogoRepositorio
        UsuarioRepositorio usuarioRepositorio
    }

    class DadosInvalidosException {
        <<exception>>
    }

    class Catalogo
    class Midia {
        <<abstract>>
    }
    class Usuario

    CatalogoRepositorio <|.. CatalogoJsonRepositorio
    UsuarioRepositorio <|.. UsuarioJsonRepositorio

    CatalogoJsonRepositorio ..> MidiaFactory : usa
    MidiaFactory ..> Midia : cria
    UsuarioJsonRepositorio ..> ArquivoJson : usa
    UsuarioJsonRepositorio ..> Usuario : monta
    UsuarioJsonRepositorio ..> Midia : aplica as notas

    CarregadorDados ..> CatalogoJsonRepositorio : cria
    CarregadorDados ..> Catalogo : cria
    CarregadorDados ..> UsuarioJsonRepositorio : carrega
    CarregadorDados ..> DadosCarregados : devolve
    DadosCarregados --> Catalogo
    DadosCarregados --> CatalogoRepositorio
    DadosCarregados --> UsuarioRepositorio

    MidiaFactory ..> DadosInvalidosException : lança
    CatalogoJsonRepositorio ..> DadosInvalidosException : lança
    UsuarioJsonRepositorio ..> DadosInvalidosException : lança

    note for MidiaFactory "Padrão Factory: decide qual subclasse de Midia criar pelo campo tipo do JSON"
```

## 4. Interface gráfica (JavaFX)

As telas só apresentam dados e chamam os serviços; nenhuma regra de negócio fica nelas. O `App` controla a troca de telas.

```mermaid
classDiagram
    direction TB

    class Application {
        <<JavaFX>>
    }

    class App {
        -Catalogo catalogo
        -GerenciadorUsuarios gerenciador
        -Stage stage
        +start(Stage stage) void
        +mostrarLogin() void
        +mostrarCatalogo() void
        +mostrarDetalhe(Midia midia) void
        +mostrarMinhaLista() void
        +mostrarRecomendacoes() void
        +getCatalogo() Catalogo
        +getGerenciador() GerenciadorUsuarios
        +getUsuarioLogado() Usuario
        +setUsuarioLogado(Usuario usuario) void
        +main(String[] args)$ void
    }

    class TelaLogin {
        -App app
        +getRaiz() VBox
    }
    class TelaCatalogo {
        -App app
        +getRaiz() VBox
    }
    class TelaDetalhe {
        -App app
        -Midia midia
        +getRaiz() VBox
    }
    class TelaMinhaLista {
        -App app
        +getRaiz() VBox
    }
    class TelaRecomendacoes {
        -App app
        -Recomendador recomendador
        +getRaiz() VBox
    }

    class Catalogo
    class GerenciadorUsuarios
    class SessaoUsuario {
        <<Singleton>>
    }
    class Recomendador
    class RecomendacaoStrategy {
        <<interface>>
    }

    Application <|-- App

    App ..> TelaLogin : cria
    App ..> TelaCatalogo : cria
    App ..> TelaDetalhe : cria
    App ..> TelaMinhaLista : cria
    App ..> TelaRecomendacoes : cria

    TelaLogin --> App
    TelaCatalogo --> App
    TelaDetalhe --> App
    TelaMinhaLista --> App
    TelaRecomendacoes --> App

    App --> Catalogo
    App --> GerenciadorUsuarios
    App ..> SessaoUsuario : usa
    TelaRecomendacoes --> Recomendador
    TelaRecomendacoes ..> RecomendacaoStrategy : escolhe o critério
```

## Onde cada padrão e conceito aparece

| Item exigido | Onde está |
|---|---|
| Classe abstrata com subclasses concretas | `Midia` com `Filme`, `Serie` e `Documentario` |
| Interfaces usadas de forma polimórfica | `Avaliavel` (em `Midia`), `Exibivel` (nas três subclasses) e `RecomendacaoStrategy` |
| Encapsulamento | atributos privados em todas as classes do domínio |
| Padrão Factory | `MidiaFactory` |
| Padrão Singleton | `SessaoUsuario` |
| Padrão Strategy | `RecomendacaoStrategy`, `RecomendacaoPorNota`, `RecomendacaoPorGenero` e `Recomendador` |
| Tratamento de exceções | `NotaInvalidaException`, `TituloDuplicadoException`, `UsernameJaExisteException` e `DadosInvalidosException` |
| Extra (interface gráfica) | pacote `ui`, em JavaFX |
