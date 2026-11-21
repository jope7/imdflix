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

/**
 * Ponto de entrada da interface JavaFX e controlador de telas.
 *
 * Ao iniciar, carrega os dados; depois guarda o catálogo e o gerenciador de usuários, e troca a tela
 * mostrada na janela. O usuário logado fica na {@link SessaoUsuario} (Singleton), e as telas só
 * chamam os serviços, sem regras de negócio.
 */
public class App extends Application {
    /** Catálogo em memória, compartilhado por todas as telas. */
    private Catalogo catalogo;
    /** Cadastro e login de usuários. */
    private GerenciadorUsuarios gerenciador;
    /** Janela principal, onde as telas são trocadas. */
    private Stage stage;

    /**
     * Carrega os dados e mostra a tela de login. Se algum arquivo de dados estiver inválido, mostra um
     * alerta com o motivo e encerra o programa.
     *
     * @param stage a janela principal
     * @throws Exception se a interface não puder ser iniciada
     */
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

    /**
     * Troca o conteúdo da janela.
     *
     * @param raiz o painel raiz da nova tela
     */
    private void trocarTela(Parent raiz) {
        stage.setScene(new Scene(raiz, 700, 450));
    }

    /** Mostra a tela de login e encerra a sessão do usuário atual. */
    public void mostrarLogin() {
        SessaoUsuario.getInstancia().sair();
        trocarTela(new TelaLogin(this).getRaiz());
    }

    /** Mostra a tela do catálogo. */
    public void mostrarCatalogo() {
        trocarTela(new TelaCatalogo(this).getRaiz());
    }

    /**
     * Mostra a tela de detalhe de um título.
     *
     * @param midia o título a mostrar
     */
    public void mostrarDetalhe(Midia midia) {
        trocarTela(new TelaDetalhe(this, midia).getRaiz());
    }

    /** Mostra a lista pessoal do usuário logado. */
    public void mostrarMinhaLista() {
        trocarTela(new TelaMinhaLista(this).getRaiz());
    }

    /** Mostra a tela de recomendações. */
    public void mostrarRecomendacoes() {
        trocarTela(new TelaRecomendacoes(this).getRaiz());
    }

    /**
     * Informa o catálogo carregado.
     *
     * @return o catálogo em memória
     */
    public Catalogo getCatalogo() { return catalogo; }
    /**
     * Informa o gerenciador de usuários.
     *
     * @return o gerenciador de usuários
     */
    public GerenciadorUsuarios getGerenciador() { return gerenciador; }
    /**
     * Informa quem está logado, consultando a {@link SessaoUsuario}.
     *
     * @return o usuário logado
     * @throws java.util.NoSuchElementException se ninguém estiver logado
     */
    public Usuario getUsuarioLogado() { 
        return SessaoUsuario.getInstancia().getUsuarioAtual().orElseThrow();
    }
    /**
     * Registra quem acabou de entrar, na {@link SessaoUsuario}.
     *
     * @param usuario o usuário que entrou
     */
    public void setUsuarioLogado(Usuario usuario) { 
        SessaoUsuario.getInstancia().entrar(usuario);
    }

    /**
     * Inicia o programa.
     *
     * @param args argumentos da linha de comando (não são usados)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
