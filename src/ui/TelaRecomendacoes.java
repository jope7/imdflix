package ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import modelo.Midia;
import servico.RecomendacaoPorGenero;
import servico.RecomendacaoPorNota;
import servico.RecomendacaoStrategy;
import servico.Recomendador;

/**
 * Mostra recomendações para o usuário logado, em cartões; o critério (Strategy) pode ser trocado na
 * própria tela. Clicar em um cartão abre o detalhe do título.
 */
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
    /** Grade com os cartões das recomendações. */
    private final FlowPane grade = new FlowPane();

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
        titulo.getStyleClass().add("titulo-tela");

        // critério de recomendação (a estratégia em uso)
        cbEstrategia.getItems().addAll(estrategias.keySet());
        cbEstrategia.setValue(recomendador.getEstrategia().getNome());
        cbEstrategia.setOnAction(e -> trocarEstrategia());
        HBox linhaCriterio = new HBox(10, new Label("Critério:"), cbEstrategia);
        linhaCriterio.setAlignment(Pos.CENTER_LEFT);

        lblDescricao.setWrapText(true);
        lblDescricao.getStyleClass().add("descricao");

        grade.setHgap(16);
        grade.setVgap(18);
        grade.setPadding(new Insets(8));

        ScrollPane rolagem = new ScrollPane(grade);
        rolagem.setFitToWidth(true);

        raiz = new VBox(14, btnVoltar, titulo, linhaCriterio, lblDescricao, rolagem);
        VBox.setVgrow(rolagem, Priority.ALWAYS);
        raiz.setPadding(new Insets(20));

        atualizar();
    }

    /** Troca o critério de recomendação para o escolhido na caixa e atualiza os cartões. */
    private void trocarEstrategia() {
        String nome = cbEstrategia.getValue();
        if (nome == null) {
            return;
        }
        recomendador.setEstrategia(estrategias.get(nome));
        atualizar();
    }

    /** Recalcula as recomendações com o critério atual e mostra os cartões. */
    private void atualizar() {
        RecomendacaoStrategy estrategia = recomendador.getEstrategia();
        lblDescricao.setText(estrategia.getDescricao());

        List<Midia> recomendacoes = recomendador.recomendar(app.getUsuarioLogado(), app.getCatalogo(), LIMITE);

        grade.getChildren().clear();
        for (Midia midia : recomendacoes) {
            grade.getChildren().add(CartaoMidia.criar(midia, () -> app.mostrarDetalhe(midia)));
        }
        if (recomendacoes.isEmpty()) {
            Label vazio = new Label("Sem recomendações por enquanto. Avalie alguns títulos para receber sugestões.");
            vazio.getStyleClass().add("descricao");
            grade.getChildren().add(vazio);
        }
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