package ui;

import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import modelo.Midia;

/**
 * Tela do catálogo: mostra os títulos em cartões (capa, título, gênero, ano e nota média), com
 * busca pelo título e filtro por gênero. Um clique em um cartão abre o detalhe do título. Também
 * leva às recomendações e à lista pessoal.
 */
public class TelaCatalogo {
    /** Opção do filtro que mostra todos os gêneros. */
    private static final String TODOS = "Todos os gêneros";

    /** Controlador de telas. */
    private final App app;
    /** Painel raiz da tela. */
    private final VBox raiz;
    /** Grade com os cartões dos títulos. */
    private final FlowPane grade = new FlowPane();
    /** Campo de busca pelo título. */
    private final TextField txtBusca = new TextField();
    /** Filtro por gênero. */
    private final ComboBox<String> cbGenero = new ComboBox<>();
    /** Mensagem de erro. */
    private final Label lblErro = new Label();

    /**
     * Monta a tela.
     *
     * @param app o controlador de telas
     */
    public TelaCatalogo(App app) {
        this.app = app;

        // topo: logo, saudação e navegação
        Label logo = new Label("IMDFLIX");
        logo.getStyleClass().add("logo-topo");
        Label ola = new Label("Olá, " + app.getUsuarioLogado().getNome());
        ola.getStyleClass().add("saudacao");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Button btnRecomendacoes = new Button("Recomendações");
        btnRecomendacoes.setOnAction(e -> app.mostrarRecomendacoes());
        Button btnMinhaLista = new Button("Minha lista");
        btnMinhaLista.setOnAction(e -> app.mostrarMinhaLista());
        Button btnSair = new Button("Sair");
        btnSair.setOnAction(e -> app.mostrarLogin());

        HBox topo = new HBox(12, logo, espaco, ola, btnRecomendacoes, btnMinhaLista, btnSair);
        topo.setAlignment(Pos.CENTER_LEFT);

        // busca (o Enter também busca)
        txtBusca.setPromptText("Buscar título");
        txtBusca.setPrefWidth(250);
        txtBusca.setOnAction(e -> buscar());
        Button btnBuscar = new Button("Buscar");
        btnBuscar.getStyleClass().add("primario");
        btnBuscar.setOnAction(e -> buscar());

        // filtro por gênero: "Todos" + cada gênero que existe no catálogo
        cbGenero.getItems().add(TODOS);
        for (Midia m : app.getCatalogo().listarTodos()) {
            if (!cbGenero.getItems().contains(m.getGenero())) {
                cbGenero.getItems().add(m.getGenero());
            }
        }
        cbGenero.setValue(TODOS);
        cbGenero.setOnAction(e -> filtrarPorGenero());

        HBox filtros = new HBox(10, txtBusca, btnBuscar, cbGenero);
        filtros.setAlignment(Pos.CENTER_LEFT);

        // grade de cartões dentro de uma área com rolagem
        grade.setHgap(16);
        grade.setVgap(18);
        grade.setPadding(new Insets(8));
        mostrarTitulos(app.getCatalogo().listarTodos());

        ScrollPane rolagem = new ScrollPane(grade);
        rolagem.setFitToWidth(true);

        lblErro.getStyleClass().add("erro");

        raiz = new VBox(14, topo, filtros, lblErro, rolagem);
        VBox.setVgrow(rolagem, Priority.ALWAYS);
        raiz.setPadding(new Insets(20));
    }

    /**
     * Troca os cartões mostrados na grade.
     *
     * @param midias os títulos a mostrar
     */
    private void mostrarTitulos(List<Midia> midias) {
        grade.getChildren().clear();
        for (Midia midia : midias) {
            grade.getChildren().add(CartaoMidia.criar(midia, () -> app.mostrarDetalhe(midia)));
        }
        if (midias.isEmpty()) {
            Label vazio = new Label("Nenhum título para mostrar.");
            vazio.getStyleClass().add("card-info");
            grade.getChildren().add(vazio);
        }
    }

    /** Procura o título digitado e abre o seu detalhe. Se não existir, mostra o erro. */
    private void buscar() {
        Optional<Midia> resultado = app.getCatalogo().buscarPorTitulo(txtBusca.getText());

        if (resultado.isEmpty()) {
            lblErro.setText("Título não encontrado.");
        } else {
            Midia midia = resultado.get();
            lblErro.setText("");
            app.mostrarDetalhe(midia);
        }
    }

    /** Mostra na tabela só os títulos do gênero escolhido (ou todos). */
    private void filtrarPorGenero() {
        String genero = cbGenero.getValue();
        List<Midia> lista;
        if (genero.equals(TODOS)){
            lista = app.getCatalogo().listarTodos();
        } else {
            lista = app.getCatalogo().filtrarPorGenero(genero);
        } 
        mostrarTitulos(lista);
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