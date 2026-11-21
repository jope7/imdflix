package ui;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import modelo.Usuario;
import servico.CarregadorDados;
import servico.Catalogo;
import servico.GerenciadorUsuarios;
import modelo.Midia;
import servico.SessaoUsuario;

public class App extends Application {
    private Catalogo catalogo;
    private GerenciadorUsuarios gerenciador;
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        try {
            var dados = CarregadorDados.padrao().carregar();
            this.catalogo = dados.catalogo();
            this.gerenciador = new GerenciadorUsuarios(dados.usuarioRepositorio());
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Nao foi possivel carregar os dados:\n" + e.getMessage()).showAndWait();
            Platform.exit();
            return;
        }
        stage.setTitle("IMDFlix");
        mostrarLogin();
        stage.show();
    }

    private void trocarTela(Parent raiz) {
        stage.setScene(new Scene(raiz, 700, 450));
    }

    public void mostrarLogin() {
        SessaoUsuario.getInstancia().sair();
        trocarTela(new TelaLogin(this).getRaiz());
    }

    public void mostrarCatalogo() {
        trocarTela(new TelaCatalogo(this).getRaiz());
    }

    public void mostrarDetalhe(Midia midia) {
        trocarTela(new TelaDetalhe(this, midia).getRaiz());
    }

    public void mostrarMinhaLista() {
        trocarTela(new TelaMinhaLista(this).getRaiz());
    }

    public void mostrarRecomendacoes() {
        trocarTela(new TelaRecomendacoes(this).getRaiz());
    }

    public Catalogo getCatalogo() { return catalogo; }
    public GerenciadorUsuarios getGerenciador() { return gerenciador; }
    public Usuario getUsuarioLogado() { 
        return SessaoUsuario.getInstancia().getUsuarioAtual().orElseThrow();
    }
    public void setUsuarioLogado(Usuario usuario) { 
        SessaoUsuario.getInstancia().entrar(usuario);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
