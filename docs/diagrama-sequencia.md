# Diagramas de sequência: IMDFlix

Os fluxos foram montados a partir do código do repositório. O GitHub renderiza os blocos `mermaid` automaticamente. Para gerar imagens para os slides, cole o código em https://mermaid.live.

## 1. Usuário avalia um título

É o fluxo principal do sistema: mostra a tela, a sessão (Singleton), o modelo, a validação com exceção e a gravação no arquivo JSON.

```mermaid
sequenceDiagram
    actor Pessoa
    participant TelaDetalhe
    participant SessaoUsuario
    participant Usuario
    participant Midia
    participant GerenciadorUsuarios
    participant UsuarioJsonRepositorio
    participant ArquivoJson

    Pessoa->>TelaDetalhe: digita a nota e clica em Avaliar
    TelaDetalhe->>TelaDetalhe: converte o texto em número (vírgula vira ponto)
    TelaDetalhe->>SessaoUsuario: getUsuarioAtual()
    SessaoUsuario-->>TelaDetalhe: usuário logado

    TelaDetalhe->>Usuario: avaliar(midia, nota)
    Usuario->>Midia: avaliar(nota) ou substituirAvaliacao(notaAnterior, nota)
    Note right of Midia: se o usuário já tinha avaliado,<br/>a nota antiga é trocada pela nova

    alt nota entre 0 e 10
        Midia-->>Usuario: média recalculada
        Usuario->>Usuario: guarda a nota no mapa de avaliações
        TelaDetalhe->>GerenciadorUsuarios: salvar(usuario)
        GerenciadorUsuarios->>UsuarioJsonRepositorio: salvar(usuario)
        UsuarioJsonRepositorio->>ArquivoJson: gravarLista(caminho, usuários)
        ArquivoJson-->>UsuarioJsonRepositorio: arquivo gravado
        UsuarioJsonRepositorio-->>GerenciadorUsuarios: ok
        GerenciadorUsuarios-->>TelaDetalhe: ok
        TelaDetalhe->>Pessoa: mostra Avaliação registrada e a nova média
    else nota fora do intervalo
        Midia-->>Usuario: NotaInvalidaException
        Usuario-->>TelaDetalhe: NotaInvalidaException
        TelaDetalhe->>Pessoa: mostra a mensagem de erro (nada é salvo)
    end
```

Outros erros tratados pela tela: texto que não é número (`NumberFormatException`, com a mensagem "Digite um número") e falha ao gravar o arquivo (`IOException`, com a mensagem "Erro ao salvar os dados").

## 2. Inicialização: carregar os dados

Mostra a ordem obrigatória: o catálogo é carregado **antes** dos usuários, porque as notas dos usuários são aplicadas nas mídias do catálogo para recalcular as médias. Títulos que não existem mais no catálogo são ignorados.

```mermaid
sequenceDiagram
    participant App
    participant CarregadorDados
    participant Catalogo
    participant CatalogoJsonRepositorio
    participant MidiaFactory
    participant UsuarioJsonRepositorio
    participant ArquivoJson
    participant Midia
    participant GerenciadorUsuarios

    App->>CarregadorDados: padrao() e carregar()

    alt arquivos válidos
        Note over CarregadorDados,MidiaFactory: Etapa 1: catálogo
        CarregadorDados->>CatalogoJsonRepositorio: new CatalogoJsonRepositorio(catalogo.json)
        CarregadorDados->>Catalogo: new Catalogo(catalogoRepositorio)
        Catalogo->>CatalogoJsonRepositorio: listarTodos()
        loop para cada título do arquivo
            CatalogoJsonRepositorio->>MidiaFactory: criar(objeto JSON)
            MidiaFactory-->>CatalogoJsonRepositorio: Filme, Serie ou Documentario
        end
        CatalogoJsonRepositorio-->>Catalogo: lista de mídias

        Note over CarregadorDados,Midia: Etapa 2: usuários (só depois do catálogo)
        CarregadorDados->>UsuarioJsonRepositorio: carregar(usuarios.json, catalogo.buscarPorTitulo)
        UsuarioJsonRepositorio->>ArquivoJson: lerLista(caminho)
        ArquivoJson-->>UsuarioJsonRepositorio: itens JSON
        UsuarioJsonRepositorio->>UsuarioJsonRepositorio: valida todos os registros sem alterar nenhuma mídia
        loop para cada usuário e para cada título da lista ou das avaliações
            UsuarioJsonRepositorio->>Catalogo: buscarPorTitulo(titulo)
            alt título existe no catálogo
                UsuarioJsonRepositorio->>Midia: Usuario.avaliar(midia, nota) recalcula a média
            else título não existe mais
                Note over UsuarioJsonRepositorio: o título é ignorado
            end
        end
        UsuarioJsonRepositorio-->>CarregadorDados: repositório carregado
        CarregadorDados-->>App: DadosCarregados (catálogo e repositórios)

        App->>GerenciadorUsuarios: new GerenciadorUsuarios(usuarioRepositorio)
        GerenciadorUsuarios->>UsuarioJsonRepositorio: listarTodos()
        UsuarioJsonRepositorio-->>GerenciadorUsuarios: usuários em memória
        App->>App: mostrarLogin()
    else arquivo inválido (DadosInvalidosException)
        CarregadorDados-->>App: exceção com a posição e o motivo do erro
        App->>App: mostra um alerta com a mensagem e encerra o programa
    end
```

## 3. Recomendações e troca de estratégia (Strategy)

Mostra o padrão Strategy em ação: o `Recomendador` delega o cálculo à estratégia atual, e a tela pode trocá-la em tempo de execução sem alterar o `Recomendador`.

```mermaid
sequenceDiagram
    actor Pessoa
    participant TelaCatalogo
    participant App
    participant TelaRecomendacoes
    participant Recomendador
    participant Catalogo
    participant RecomendacaoPorNota
    participant RecomendacaoPorGenero

    Pessoa->>TelaCatalogo: clica em Recomendações
    TelaCatalogo->>App: mostrarRecomendacoes()
    App->>TelaRecomendacoes: new TelaRecomendacoes(app)
    TelaRecomendacoes->>Recomendador: new Recomendador(RecomendacaoPorNota)
    Note over TelaRecomendacoes: critério inicial: Mais bem avaliados

    TelaRecomendacoes->>Recomendador: recomendar(usuario, catalogo, 5)
    Recomendador->>Catalogo: listarTodos()
    Catalogo-->>Recomendador: títulos do catálogo
    Recomendador->>RecomendacaoPorNota: recomendar(usuario, títulos, 5)
    Note over RecomendacaoPorNota: exclui o que o usuário já viu<br/>e ordena pela nota média
    RecomendacaoPorNota-->>Recomendador: lista de mídias
    Recomendador-->>TelaRecomendacoes: lista de mídias
    TelaRecomendacoes->>Pessoa: mostra as recomendações

    Pessoa->>TelaRecomendacoes: escolhe o critério Pelo seu gosto
    TelaRecomendacoes->>Recomendador: setEstrategia(RecomendacaoPorGenero)
    TelaRecomendacoes->>Recomendador: recomendar(usuario, catalogo, 5)
    Recomendador->>Catalogo: listarTodos()
    Catalogo-->>Recomendador: títulos do catálogo
    Recomendador->>RecomendacaoPorGenero: recomendar(usuario, títulos, 5)
    Note over RecomendacaoPorGenero: acha os gêneros em que as notas do usuário<br/>têm média 7 ou mais e exclui o que ele já viu
    RecomendacaoPorGenero-->>Recomendador: lista de mídias
    Recomendador-->>TelaRecomendacoes: lista de mídias
    TelaRecomendacoes->>Pessoa: atualiza a lista na tela
```
