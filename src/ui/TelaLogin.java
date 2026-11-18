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

public class TelaLogin {
    private final App app;
    private final VBox raiz;
    private final TextField txtUsername = new TextField();
    private final TextField txtNome = new TextField();
    private final Label lblErro = new Label();

    public TelaLogin(App app) {
        this.app = app;

        Label titulo = new Label("IMDFlix");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        txtUsername.setPromptText("username");
        txtNome.setPromptText("nome (só para criar conta)");
        txtUsername.setMaxWidth(250);
        txtNome.setMaxWidth(250);

        Button btnEntrar = new Button("Entrar");
        Button btnCriarConta = new Button("Criar conta");
        btnEntrar.setOnAction(e -> entrar());
        btnCriarConta.setOnAction(e -> criarConta());

        lblErro.setStyle("-fx-text-fill: red;");

        HBox botoes = new HBox(10, btnEntrar, btnCriarConta);
        botoes.setAlignment(Pos.CENTER);

        raiz = new VBox(12, titulo, txtUsername, txtNome, botoes, lblErro);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(20));
    }

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

    public VBox getRaiz() {
        return raiz;
    }
}