package ui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import modelo.Usuario;
import servico.CarregadorDados;
import servico.Catalogo;
import servico.GerenciadorUsuarios;
import modelo.Midia;

public class App extends Application {
    private Catalogo catalogo;
    private GerenciadorUsuarios gerenciador;
    private Usuario usuarioLogado;
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        var dados = CarregadorDados.padrao().carregar();
        this.catalogo = dados.catalogo();
        this.gerenciador = new GerenciadorUsuarios(dados.usuarioRepositorio());
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
        trocarTela(new TelaDetalhe(this, midia).getRaiz());
    }

    public void mostrarMinhaLista() {
        trocarTela(new TelaMinhaLista(this).getRaiz());
    }

    public Catalogo getCatalogo() { return catalogo; }
    public GerenciadorUsuarios getGerenciador() { return gerenciador; }
    public Usuario getUsuarioLogado() { return usuarioLogado; }
    public void setUsuarioLogado(Usuario usuario) { this.usuarioLogado = usuario; }

    public static void main(String[] args) {
        launch(args);
    }
}
