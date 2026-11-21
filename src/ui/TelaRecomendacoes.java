package ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import modelo.Midia;
import servico.RecomendacaoPorGenero;
import servico.RecomendacaoPorNota;
import servico.RecomendacaoStrategy;
import servico.Recomendador;

/** Mostra recomendações para o usuário logado; o critério (Strategy) pode ser trocado na própria tela. */
public class TelaRecomendacoes {
    /** Quantidade de recomendações mostradas. */
    private static final int LIMITE = 5;

    /** Controlador de telas. */
    private final App app;
    /** Painel raiz da tela. */
    private final VBox raiz;
    /** Contexto do Strategy: guarda o critério em uso. */
    private final Recomendador recomendador;
    /** Critérios disponíveis, indexados pelo nome mostrado na tela. */
    private final Map<String, RecomendacaoStrategy> estrategias = new LinkedHashMap<>();
    /** Caixa de seleção do critério. */
    private final ComboBox<String> cbEstrategia = new ComboBox<>();
    /** Explicação do critério em uso. */
    private final Label lblDescricao = new Label();
    /** Recomendações mostradas. */
    private final ListView<String> lista = new ListView<>();
    /** Mensagem de erro. */
    private final Label lblErro = new Label();
    /** Recomendações atuais, na mesma ordem das linhas da lista. */
    private List<Midia> atuais = List.of();

    /**
     * Monta a tela, com o critério "Mais bem avaliados" selecionado.
     *
     * @param app o controlador de telas
     */
    public TelaRecomendacoes(App app) {
        this.app = app;

        for (RecomendacaoStrategy estrategia : List.of(new RecomendacaoPorNota(), new RecomendacaoPorGenero())) {
            estrategias.put(estrategia.getNome(), estrategia);
        }
        this.recomendador = new Recomendador(estrategias.values().iterator().next());

        Button btnVoltar = new Button("← Voltar ao catálogo");
        btnVoltar.setOnAction(e -> app.mostrarCatalogo());

        Label titulo = new Label("Recomendações para " + app.getUsuarioLogado().getNome());
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // critério de recomendação (a estratégia em uso)
        cbEstrategia.getItems().addAll(estrategias.keySet());
        cbEstrategia.setValue(recomendador.getEstrategia().getNome());
        cbEstrategia.setOnAction(e -> trocarEstrategia());
        HBox linhaCriterio = new HBox(10, new Label("Critério:"), cbEstrategia);

        lblDescricao.setWrapText(true);

        lista.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                abrirSelecionado();
            }
        });
        Button btnAbrir = new Button("Ver detalhes");
        btnAbrir.setOnAction(e -> abrirSelecionado());

        lblErro.setStyle("-fx-text-fill: red;");

        raiz = new VBox(10, btnVoltar, titulo, linhaCriterio, lblDescricao, lista, btnAbrir, lblErro);
        VBox.setVgrow(lista, Priority.ALWAYS);
        raiz.setPadding(new Insets(15));

        atualizar();
    }

    /** Troca o critério de recomendação para o escolhido na caixa e atualiza a lista. */
    private void trocarEstrategia() {
        String nome = cbEstrategia.getValue();
        if (nome == null) {
            return;
        }
        recomendador.setEstrategia(estrategias.get(nome));
        atualizar();
    }

    /** Recalcula as recomendações com o critério atual e atualiza a lista. */
    private void atualizar() {
        RecomendacaoStrategy estrategia = recomendador.getEstrategia();
        lblDescricao.setText(estrategia.getDescricao());
        lblErro.setText("");

        atuais = recomendador.recomendar(app.getUsuarioLogado(), app.getCatalogo(), LIMITE);

        List<String> linhas = new ArrayList<>();
        for (Midia m : atuais) {
            String nota = m.getQuantidadeAvaliacoes() == 0
                    ? "sem avaliações"
                    : String.format("nota média %.1f", m.getNotaMedia());
            linhas.add(String.format("%s · %s · %s · %s", m.getTitulo(), m.getTipo(), m.getGenero(), nota));
        }
        lista.setPlaceholder(new Label("Sem recomendações por enquanto. " + estrategia.getDescricao()));
        lista.setItems(FXCollections.observableArrayList(linhas));
    }

    /** Abre o detalhe do título selecionado. Mostra o erro se nada estiver selecionado. */
    private void abrirSelecionado() {
        int indice = lista.getSelectionModel().getSelectedIndex();
        if (indice < 0 || indice >= atuais.size()) {
            lblErro.setText("Selecione um título.");
            return;
        }
        app.mostrarDetalhe(atuais.get(indice));
    }

    /**
     * Informa o painel da tela.
     *
     * @return o painel raiz
     */
    public VBox getRaiz() {
        return raiz;
    }
}
