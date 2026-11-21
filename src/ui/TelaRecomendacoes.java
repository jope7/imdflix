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
    private static final int LIMITE = 5;

    private final App app;
    private final VBox raiz;
    private final Recomendador recomendador;
    private final Map<String, RecomendacaoStrategy> estrategias = new LinkedHashMap<>();
    private final ComboBox<String> cbEstrategia = new ComboBox<>();
    private final Label lblDescricao = new Label();
    private final ListView<String> lista = new ListView<>();
    private final Label lblErro = new Label();
    private List<Midia> atuais = List.of();

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

    private void trocarEstrategia() {
        String nome = cbEstrategia.getValue();
        if (nome == null) {
            return;
        }
        recomendador.setEstrategia(estrategias.get(nome));
        atualizar();
    }

    // recalcula as recomendações com a estratégia atual
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

    private void abrirSelecionado() {
        int indice = lista.getSelectionModel().getSelectedIndex();
        if (indice < 0 || indice >= atuais.size()) {
            lblErro.setText("Selecione um título.");
            return;
        }
        app.mostrarDetalhe(atuais.get(indice));
    }

    public VBox getRaiz() {
        return raiz;
    }
}
