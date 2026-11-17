package ui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import modelo.Usuario;
import servico.Catalogo;
import servico.GerenciadorUsuarios;
import modelo.Midia;

public class App extends Application {
    private final Catalogo catalogo = new Catalogo();
    private final GerenciadorUsuarios gerenciador = new GerenciadorUsuarios();
    private Usuario usuarioLogado;
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        DadosIniciais.carregar(catalogo, gerenciador);
        stage.setTitle("IMDFlix");
        mostrarLogin();
        stage.show();
    }

    private void trocarTela(Parent raiz) {
        stage.setScene(new Scene(raiz, 700, 450));
    }

    public void mostrarLogin() {
        usuarioLogado = null;
        trocarTela(new TelaLogin(this).getRaiz());
    }

    public void mostrarCatalogo() {
        trocarTela(new TelaCatalogo(this).getRaiz());
    }

    public void mostrarDetalhe(Midia midia) {
        // provisório: vai ser substituída por TelaDetalhe
        trocarTela(new Label("Detalhe de " + midia.getTitulo() + " (em construção)"));
    }

    public void mostrarMinhaLista() {
        // provisório: vai ser substituída por TelaMinhaLista
        trocarTela(new Label("Minha lista (em construção)"));
    }

    public Catalogo getCatalogo() { return catalogo; }
    public GerenciadorUsuarios getGerenciador() { return gerenciador; }
    public Usuario getUsuarioLogado() { return usuarioLogado; }
    public void setUsuarioLogado(Usuario usuario) { this.usuarioLogado = usuario; }

    public static void main(String[] args) {
        launch(args);
    }
}
