package ui;

import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import modelo.Midia;
import java.io.IOException;

/**
 * Tela da lista pessoal ("Minha lista") do usuário logado: mostra os títulos guardados e permite
 * abrir o detalhe de um deles ou removê-lo da lista.
 */
public class TelaMinhaLista {
    /** Controlador de telas. */
    private final App app;
    /** Painel raiz da tela. */
    private final VBox raiz;
    /** Títulos da lista do usuário. */
    private final ListView<String> lista = new ListView<>();
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

        Label titulo = new Label("Minha lista — " + app.getUsuarioLogado().getNome());
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // lista de títulos
        lista.setPlaceholder(new Label("Sua lista está vazia. Adicione títulos pela tela de detalhe."));
        lista.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                abrirSelecionado();
            }
        });
        atualizarLista();

        // botões
        Button btnAbrir = new Button("Ver detalhes");
        btnAbrir.setOnAction(e -> abrirSelecionado());
        Button btnRemover = new Button("Remover da lista");
        btnRemover.setOnAction(e -> removerSelecionado());
        HBox botoes = new HBox(10, btnAbrir, btnRemover);

        lblErro.setStyle("-fx-text-fill: red;");

        raiz = new VBox(10, btnVoltar, titulo, lista, botoes, lblErro);
        VBox.setVgrow(lista, Priority.ALWAYS);
        raiz.setPadding(new Insets(15));
    }

    /** Recarrega os títulos da lista do usuário logado. */
    private void atualizarLista() {
        lista.setItems(FXCollections.observableArrayList(app.getUsuarioLogado().getMinhaLista()));
    }

    /** Abre o detalhe do título selecionado. Mostra o erro se nada estiver selecionado ou se o título não existir mais. */
    private void abrirSelecionado() {
        String titulo = lista.getSelectionModel().getSelectedItem();
        if (titulo == null) {
            lblErro.setText("Selecione um título");
            return;
        }

        Optional<Midia> resultado = app.getCatalogo().buscarPorTitulo(titulo);
        if (resultado.isEmpty()) {
            lblErro.setText("Este título não está mais no catálogo.");
        } else {
            app.mostrarDetalhe(resultado.get());
        }
    }

    /** Remove o título selecionado da lista e salva o usuário. Mostra o erro se nada estiver selecionado ou não for possível salvar. */
    private void removerSelecionado() {
        String titulo = lista.getSelectionModel().getSelectedItem();
        if (titulo == null) {
            lblErro.setText("Selecione um título.");
            return;
        }

        Optional<Midia> resultado = app.getCatalogo().buscarPorTitulo(titulo);
        if (resultado.isEmpty()) {
            lblErro.setText("Este título não está mais no catálogo.");
        } else {
            try {
                app.getUsuarioLogado().removerDaLista(resultado.get());
                app.getGerenciador().salvar(app.getUsuarioLogado());
                atualizarLista();
                lblErro.setText("");
            } catch (IOException e) {
                lblErro.setText("Erro ao salvar os dados do usuário.");
            }
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
