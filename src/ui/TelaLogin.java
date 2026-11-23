package ui;

import excecoes.UsernameJaExisteException;

import java.io.IOException;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modelo.Usuario;

/**
 * Tela inicial: entrar com um username existente ou criar uma conta nova (username e nome).
 *
 * Os erros (username inexistente, em formato inválido ou já em uso) aparecem na própria tela.
 */
public class TelaLogin {
    /** Controlador de telas. */
    private final App app;
    /** Painel raiz da tela. */
    private final VBox raiz;
    /** Campo do username. */
    private final TextField txtUsername = new TextField();
    /** Campo do nome (só usado ao criar conta). */
    private final TextField txtNome = new TextField();
    /** Mensagem de erro. */
    private final Label lblErro = new Label();

    /**
     * Monta a tela.
     *
     * @param app o controlador de telas
     */
    public TelaLogin(App app) {
        this.app = app;

        Label titulo = new Label("IMDFlix");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        titulo.getStyleClass().add("logo");

        txtUsername.setPromptText("username");
        txtNome.setPromptText("nome (só para criar conta)");
        txtUsername.setMaxWidth(250);
        txtNome.setMaxWidth(250);

        Button btnEntrar = new Button("Entrar");
        Button btnCriarConta = new Button("Criar conta");
        btnEntrar.setOnAction(e -> entrar());
        btnCriarConta.setOnAction(e -> criarConta());
        btnEntrar.getStyleClass().add("primario");

        lblErro.setStyle("-fx-text-fill: red;");

        HBox botoes = new HBox(10, btnEntrar, btnCriarConta);
        botoes.setAlignment(Pos.CENTER);

        raiz = new VBox(12, titulo, txtUsername, txtNome, botoes, lblErro);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(20));
    }

    /** Entra com o username digitado. Se ele não existir ou tiver formato inválido, mostra o erro. */
    private void entrar() {
        try {
            Optional <Usuario> resultado = app.getGerenciador().entrar(txtUsername.getText());
            if (resultado.isEmpty()) {
                lblErro.setText("Usuário não encontrado");
            } else {
                Usuario usuario = resultado.get();
                app.setUsuarioLogado(usuario);
                app.mostrarCatalogo();
            }
        } catch (IllegalArgumentException e){
                lblErro.setText(e.getMessage());
            }
    }

    /** Cria uma conta com o username e o nome digitados e já entra com ela. Mostra o erro se o username for inválido ou já existir. */
    private void criarConta() {
        try {
            Usuario novo = app.getGerenciador().cadastrar(txtUsername.getText(), txtNome.getText());
            app.setUsuarioLogado(novo);
            app.mostrarCatalogo();
        } catch (UsernameJaExisteException | IllegalArgumentException e) {
            lblErro.setText(e.getMessage());
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