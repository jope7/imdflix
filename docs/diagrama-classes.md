# Diagrama de classes: IMDFlix

Para editar e exportar como imagem, cole o código em https://mermaid.live.

## 1. Estado atual (para implementar)

```mermaid
classDiagram
    direction TB

    class Avaliavel {
        <<interface>>
        +avaliar(double nota) void
        +removerAvaliacao(double nota) void
        +substituirAvaliacao(double antiga, double nova) void
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
    }

    class Serie {
        -int temporadas
        -int episodiosPorTemporada
        -int duracaoMediaEpisodio
    }

    class Documentario {
        -int duracaoMinutos
        -String tema
    }

    class Usuario {
        -String username
        -String nome
        -List~String~ minhaLista
        -Map~String, Double~ avaliacoes
        +restaurar(String username, String nome, List~String~ minhaLista, Map~String, Double~ avaliacoes)$ Usuario
        +normalizarUsername(String username)$ String
        +adicionarNaLista(Midia midia) void
        +removerDaLista(Midia midia) void
        +avaliar(Midia midia, double nota) void
        +getUsername() String
        +getNome() String
    }

    class Catalogo {
        -CatalogoRepositorio repositorio
        -Map~String, Midia~ midias
        +Catalogo(CatalogoRepositorio repositorio)
        +cadastrar(Midia midia) void
        +buscarPorTitulo(String titulo) Optional~Midia~
        +filtrarPorGenero(String genero) List~Midia~
        +listarTodos() List~Midia~
        +aplicarAvaliacoes(Collection~Usuario~ usuarios) void
    }

    class GerenciadorUsuarios {
        -UsuarioRepositorio repositorio
        -Map~String, Usuario~ usuarios
        +GerenciadorUsuarios(UsuarioRepositorio repositorio)
        +cadastrar(String username, String nome) Usuario
        +salvar(Usuario usuario) void
        +entrar(String username) Optional~Usuario~
        +listarTodos() List~Usuario~
    }

    class NotaInvalidaException {
        <<exception>>
    }
    class TituloDuplicadoException {
        <<exception>>
    }
    class UsernameJaExisteException {
        <<exception>>
    }

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

    class CatalogoJsonRepositorio

    Avaliavel <|.. Midia
    Midia <|-- Filme
    Midia <|-- Serie
    Midia <|-- Documentario
    Exibivel <|.. Filme
    Exibivel <|.. Serie
    Exibivel <|.. Documentario

    Usuario ..> Midia : avalia
    Catalogo o-- Midia : contém
    GerenciadorUsuarios o-- Usuario : contém
    Catalogo --> CatalogoRepositorio : recebe no construtor
    GerenciadorUsuarios --> UsuarioRepositorio : recebe no construtor
    CatalogoRepositorio <|.. CatalogoJsonRepositorio
    Catalogo ..> Usuario : aplicarAvaliacoes

    Midia ..> NotaInvalidaException : lança
    Catalogo ..> TituloDuplicadoException : lança
    GerenciadorUsuarios ..> UsernameJaExisteException : lança

    CatalogoRepositorio ..> Midia : usa
    UsuarioRepositorio ..> Usuario : usa
```

## 2. Versão alvo (oque ainda falta)

Persistência em JSON (com Singleton e Factory) (JOACKYM), 
recomendação (Strategy) (TREMBO)
e as telas em JavaFX (VITINHO). 
`Filme`, `Serie` e `Documentario` aparecem sem atributos para simplificar.

```mermaid
classDiagram
    direction TB

    class Midia {
        <<abstract>>
    }
    class Usuario
    class Catalogo
    class GerenciadorUsuarios

    class CatalogoRepositorio {
        <<interface>>
    }
    class UsuarioRepositorio {
        <<interface>>
    }

    class CatalogoJsonRepositorio {
        -static CatalogoJsonRepositorio instancia
        -String caminhoArquivo
        +getInstancia() CatalogoJsonRepositorio
    }

    class UsuarioJsonRepositorio {
        -String caminhoArquivo
    }

    class MidiaFactory {
        +criar(String tipo, Map dados) Midia
    }

    class RecomendacaoStrategy {
        <<interface>>
        +recomendar(List~Midia~ midias) List~Midia~
    }
    class RecomendacaoPorGenero
    class RecomendacaoPorNota

    class TelaPrincipal {
        <<JavaFX>>
    }

    CatalogoRepositorio <|.. CatalogoJsonRepositorio
    UsuarioRepositorio <|.. UsuarioJsonRepositorio
    CatalogoJsonRepositorio ..> MidiaFactory : usa
    MidiaFactory ..> Midia : cria
    Catalogo ..> RecomendacaoStrategy : usa
    RecomendacaoStrategy <|.. RecomendacaoPorGenero
    RecomendacaoStrategy <|.. RecomendacaoPorNota
    TelaPrincipal --> Catalogo : usa
    TelaPrincipal --> GerenciadorUsuarios : usa
    TelaPrincipal --> Usuario : usa
```

## Padrões de projeto no diagrama

- **Singleton:** `CatalogoJsonRepositorio` (acesso único ao arquivo JSON).
- **Factory:** `MidiaFactory` (cria a subclasse correta de `Midia` a partir dos dados do JSON).
- **Strategy:** `RecomendacaoStrategy` e suas implementações (algoritmos de recomendação intercambiáveis).