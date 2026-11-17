package ui;

import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import modelo.Midia;

public class TelaCatalogo {
    private static final String TODOS = "Todos os gêneros";

    private final App app;
    private final VBox raiz;
    private final TableView<Midia> tabela = new TableView<>();
    private final TextField txtBusca = new TextField();
    private final ComboBox<String> cbGenero = new ComboBox<>();
    private final Label lblErro = new Label();

    public TelaCatalogo(App app) {
        this.app = app;

        Label titulo = new Label("Catálogo — olá, " + app.getUsuarioLogado().getNome());
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // busca
        txtBusca.setPromptText("título");
        Button btnBuscar = new Button("Buscar");
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

        // navegação
        Button btnMinhaLista = new Button("Minha lista");
        btnMinhaLista.setOnAction(e -> app.mostrarMinhaLista());
        Button btnSair = new Button("Sair");
        btnSair.setOnAction(e -> app.mostrarLogin());

        HBox barra = new HBox(10, txtBusca, btnBuscar, cbGenero, btnMinhaLista, btnSair);

        // tabela
        criarColunas();
        tabela.setItems(FXCollections.observableArrayList(app.getCatalogo().listarTodos()));
        tabela.setRowFactory(tv -> {
            TableRow<Midia> linha = new TableRow<>();
            linha.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !linha.isEmpty()) {
                    app.mostrarDetalhe(linha.getItem());
                }
            });
            return linha;
        });

        lblErro.setStyle("-fx-text-fill: red;");

        raiz = new VBox(10, titulo, barra, tabela, lblErro);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        raiz.setPadding(new Insets(15));
    }

    private void criarColunas() {
        TableColumn<Midia, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));

        TableColumn<Midia, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitulo()));

        TableColumn<Midia, String> colGenero = new TableColumn<>("Gênero");
        colGenero.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGenero()));

        TableColumn<Midia, String> colNota = new TableColumn<>("Nota média");
        colNota.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.1f", c.getValue().getNotaMedia())));

        tabela.getColumns().addAll(List.of(colTipo, colTitulo, colGenero, colNota));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

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

    private void filtrarPorGenero() {
        String genero = cbGenero.getValue();
        List<Midia> lista;
        if (genero.equals(TODOS)){
            lista = app.getCatalogo().listarTodos();
        } else {
            lista = app.getCatalogo().filtrarPorGenero(genero);
        } 
        tabela.setItems(FXCollections.observableArrayList(lista));
    }

    public VBox getRaiz() {
        return raiz;
    }
}
