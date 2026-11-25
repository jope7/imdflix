package ui;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import modelo.Midia;

/**
 * Tela da lista pessoal ("Minha lista") do usuário logado: mostra os títulos guardados e permite
 * abrir o detalhe de um deles ou removê-lo da lista.
 */
public class TelaMinhaLista {
    /** Controlador de telas. */
    private final App app;
    /** Painel raiz da tela. */
    private final VBox raiz;
    /** Grade com os cartões dos títulos da lista. */
    private final FlowPane grade = new FlowPane();
    /** Quantidade de títulos na lista. */
    private final Label lblQuantidade = new Label();
    /** Mensagem de erro. */
    private final Label lblErro = new Label();

    /**
     * Monta a tela.
     *
     * @param app o controlador de telas
     */
    public TelaMinhaLista(App app) {
        this.app = app;

        Button btnVoltar = new Button("← Voltar ao catálogo");
        btnVoltar.setOnAction(e -> app.mostrarCatalogo());

                Label titulo = new Label("Minha lista");
        titulo.getStyleClass().add("titulo-tela");
        lblQuantidade.getStyleClass().add("descricao");
        HBox cabecalho = new HBox(12, titulo, lblQuantidade);
        cabecalho.setAlignment(Pos.BASELINE_LEFT);

        grade.setHgap(16);
        grade.setVgap(18);
        grade.setPadding(new Insets(8));

        ScrollPane rolagem = new ScrollPane(grade);
        rolagem.setFitToWidth(true);

        lblErro.getStyleClass().add("erro");

        raiz = new VBox(14, btnVoltar, cabecalho, lblErro, rolagem);
        VBox.setVgrow(rolagem, Priority.ALWAYS);
        raiz.setPadding(new Insets(20));

        atualizarLista();
    }

        /** Recarrega os cartões com os títulos da lista do usuário logado. */
    private void atualizarLista() {
        List<String> titulos = app.getUsuarioLogado().getMinhaLista();
        grade.getChildren().clear();

        for (String titulo : titulos) {
            Optional<Midia> resultado = app.getCatalogo().buscarPorTitulo(titulo);
            if (resultado.isPresent()) {
                grade.getChildren().add(criarItem(resultado.get()));
            }
        }

        if (titulos.isEmpty()) {
            Label vazio = new Label("Sua lista está vazia. Adicione títulos pela tela de detalhe.");
            vazio.getStyleClass().add("descricao");
            grade.getChildren().add(vazio);
        }
        lblQuantidade.setText(titulos.size() == 1 ? "1 título" : titulos.size() + " títulos");
    }

    /**
     * Monta um item da lista: o cartão do título (que abre o detalhe) e o botão de remover.
     *
     * @param midia o título
     * @return o item
     */
    private VBox criarItem(Midia midia) {
        VBox cartao = CartaoMidia.criar(midia, () -> app.mostrarDetalhe(midia));

        Button btnRemover = new Button("Remover");
        btnRemover.getStyleClass().add("remover");
        btnRemover.setMaxWidth(CartaoMidia.LARGURA);
        btnRemover.setOnAction(e -> remover(midia));

        return new VBox(8, cartao, btnRemover);
    }

    /**
     * Remove o título da lista e salva o usuário. Mostra o erro se não for possível salvar.
     *
     * @param midia o título a remover
     */
    private void remover(Midia midia) {
        try {
            app.getUsuarioLogado().removerDaLista(midia);
            app.getGerenciador().salvar(app.getUsuarioLogado());
            atualizarLista();
            lblErro.setText("");
        } catch (IOException e) {
            lblErro.setText("Erro ao salvar os dados do usuário.");
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
