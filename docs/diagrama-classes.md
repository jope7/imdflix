# Diagrama de classes: IMDFlix

Para editar e exportar como imagem, cole o código em https://mermaid.live.

## 1. Estado atual (para implementar)

```mermaid
classDiagram
    direction TB

    class Avaliavel {
        <<interface>>
        +avaliar(double nota) void
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
        +avaliar(double nota) void
        +getNotaMedia() double
        +getTitulo() String
        +getGenero() String
        +getAnoLancamento() int
    }

    class Filme {
        -int duracaoMinutos
        -String diretor
        +getTipo() String
        +getDuracaoTotalMinutos() int
    }

    class Serie {
        -int temporadas
        -int episodiosPorTemporada
        -int duracaoMediaEpisodio
        +getTipo() String
        +getDuracaoTotalMinutos() int
    }

    class Documentario {
        -int duracaoMinutos
        -String tema
        +getTipo() String
        +getDuracaoTotalMinutos() int
    }

    class NotaInvalidaException {
        <<exception>>
    }

    class CatalogoRepositorio {
        <<interface>>
        +salvar(Midia midia) void
        +listarTodos() List~Midia~
        +buscarPorTitulo(String titulo) Optional~Midia~
    }

    Avaliavel <|.. Midia
    Midia <|-- Filme
    Midia <|-- Serie
    Midia <|-- Documentario
    Exibivel <|.. Filme
    Exibivel <|.. Serie
    Exibivel <|.. Documentario
    Midia ..> NotaInvalidaException : lança
    CatalogoRepositorio ..> Midia : usa
```

## 2. Versão alvo

Classes a serem adicionadas `Usuario`, `Catalogo`, `CatalogoJsonRepositorio`, `MidiaFactory`, `RecomendacaoStrategy` e a camada de interface em JavaFX. Para simplificar, `Filme`, `Serie` e `Documentario` aparecem sem atributos.

```mermaid
classDiagram
    direction TB

    class Midia {
        <<abstract>>
        -String titulo
        -String genero
        +getTipo()* String
        +avaliar(double nota) void
    }
    class Filme
    class Serie
    class Documentario

    class Usuario {
        -String nome
        -List~Midia~ minhaLista
        +adicionarNaLista(Midia midia) void
        +removerDaLista(Midia midia) void
        +getMinhaLista() List~Midia~
    }

    class Catalogo {
        -List~Midia~ midias
        +cadastrar(Midia midia) void
        +buscarPorTitulo(String titulo) Midia
        +filtrarPorGenero(String genero) List~Midia~
        +recomendar(RecomendacaoStrategy estrategia) List~Midia~
    }

    class CatalogoRepositorio {
        <<interface>>
        +salvar(Midia midia) void
        +listarTodos() List~Midia~
        +buscarPorTitulo(String titulo) Optional~Midia~
    }

    class CatalogoJsonRepositorio {
        -static CatalogoJsonRepositorio instancia
        -String caminhoArquivo
        +getInstancia() CatalogoJsonRepositorio
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

    Midia <|-- Filme
    Midia <|-- Serie
    Midia <|-- Documentario

    Catalogo o-- Midia : contém
    Usuario o-- Midia : minhaLista
    Catalogo --> CatalogoRepositorio : persiste via
    CatalogoRepositorio <|.. CatalogoJsonRepositorio
    MidiaFactory ..> Midia : cria
    CatalogoJsonRepositorio ..> MidiaFactory : usa
    Catalogo ..> RecomendacaoStrategy : usa
    RecomendacaoStrategy <|.. RecomendacaoPorGenero
    RecomendacaoStrategy <|.. RecomendacaoPorNota
    TelaPrincipal --> Catalogo : usa
    TelaPrincipal --> Usuario : usa
```

## Padrões de projeto no diagrama

- **Singleton:** `CatalogoJsonRepositorio` (acesso único ao arquivo JSON).
- **Factory:** `MidiaFactory` (cria a subclasse correta de `Midia` a partir dos dados do JSON).
- **Strategy:** `RecomendacaoStrategy` e suas implementações (algoritmos de recomendação intercambiáveis).
