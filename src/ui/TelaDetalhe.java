package ui;

import excecoes.NotaInvalidaException;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modelo.Exibivel;
import modelo.Midia;
import java.io.IOException;

/**
 * Tela de detalhe de um título: mostra os dados, o resumo de exibição, a nota média e a nota do
 * usuário, e permite avaliar o título (nota de 0 a 10, aceitando vírgula) e adicioná-lo à lista.
 *
 * Depois de cada mudança, o usuário é salvo pelo {@code GerenciadorUsuarios}.
 */
public class TelaDetalhe {
    /** Controlador de telas. */
    private final App app;
    /** Título mostrado. */
    private final Midia midia;
    /** Painel raiz da tela. */
    private final VBox raiz;
    /** Texto da nota média. */
    private final Label lblMedia = new Label();
    /** Texto da nota dada pelo usuário. */
    private final Label lblMinhaNota = new Label();
    /** Campo onde o usuário digita a nota. */
    private final TextField txtNota = new TextField();
    /** Botão de adicionar à lista; fica desativado se o título já estiver na lista. */
    private final Button btnAdicionar = new Button("Adicionar à minha lista");
    /** Mensagem de erro. */
    private final Label lblErro = new Label();
    /** Mensagem de sucesso. */
    private final Label lblSucesso = new Label();

    /**
     * Monta a tela.
     *
     * @param app o controlador de telas
     * @param midia o título a mostrar
     */
    public TelaDetalhe(App app, Midia midia) {
        this.app = app;
        this.midia = midia;

        Button btnVoltar = new Button("← Voltar ao catálogo");
        btnVoltar.setOnAction(e -> app.mostrarCatalogo());

        Label titulo = new Label(midia.getTitulo());
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label info = new Label(midia.getTipo() + " · " + midia.getGenero() + " · " + midia.getAnoLancamento());

        // resumo de exibição (ex.: "136 min, dirigido por Wachowski")
        String textoResumo = (midia instanceof Exibivel exibivel) ? exibivel.getResumoExibicao() : "";
        Label resumo = new Label(textoResumo);

        // avaliar
        txtNota.setPromptText("nota de 0 a 10");
        txtNota.setMaxWidth(120);
        Button btnAvaliar = new Button("Avaliar");
        btnAvaliar.getStyleClass().add("primario");
        btnAvaliar.setOnAction(e -> avaliar());
        HBox linhaAvaliar = new HBox(10, txtNota, btnAvaliar);

        // minha lista
        btnAdicionar.setOnAction(e -> adicionarNaLista());
        if (app.getUsuarioLogado().getMinhaLista().contains(midia.getTitulo())) {
            btnAdicionar.setDisable(true);
            btnAdicionar.setText("Já está na sua lista");
        }

        lblErro.setStyle("-fx-text-fill: red;");
        lblSucesso.setStyle("-fx-text-fill: green;");

        atualizarNotas();

        raiz = new VBox(10, btnVoltar, titulo, info, resumo, lblMedia, lblMinhaNota,
                        linhaAvaliar, btnAdicionar, lblErro, lblSucesso);
        raiz.setPadding(new Insets(20));
    }

    /** Atualiza os textos da nota média e da nota do usuário. */
    private void atualizarNotas() {
        lblMedia.setText(String.format("Nota média: %.1f (%d avaliação(ões))",
                midia.getNotaMedia(), midia.getQuantidadeAvaliacoes()));

        Double minhaNota = app.getUsuarioLogado().getAvaliacoes().get(midia.getTitulo());
        if (minhaNota == null) {
            lblMinhaNota.setText("Você ainda não avaliou este título.");
        } else {
            lblMinhaNota.setText(String.format("Sua nota: %.1f", minhaNota));
        }
    }

    /**
     * Registra a nota digitada e salva o usuário. Mostra o erro se a nota não for um número, se estiver
     * fora de 0 a 10 ou se não for possível salvar.
     */
    private void avaliar() {
        try {
            double nota = Double.parseDouble(txtNota.getText().trim().replace(',', '.'));
            app.getUsuarioLogado().avaliar(midia, nota);
            app.getGerenciador().salvar(app.getUsuarioLogado());
            lblErro.setText("");
            atualizarNotas();
            lblSucesso.setText("Avaliação registrada.");
        } catch (NotaInvalidaException e) {
            lblSucesso.setText("");
            lblErro.setText(e.getMessage());
        } catch (NumberFormatException e) {
            lblSucesso.setText("");
            lblErro.setText("Digite um número, ex.: 7.5");
        } catch (IOException e) {
            lblSucesso.setText("");
            lblErro.setText("Erro ao salvar os dados do usuários.");
        }
    }

    /** Adiciona o título à lista do usuário e salva. Mostra o erro se não for possível salvar. */
    private void adicionarNaLista() {
        try {
            lblErro.setText("");
            app.getUsuarioLogado().adicionarNaLista(midia);
            app.getGerenciador().salvar(app.getUsuarioLogado());
            lblSucesso.setText("Adicionado à sua lista.");
            btnAdicionar.setDisable(true);
            btnAdicionar.setText("Já está na sua lista");
        } catch (IOException e) {
            lblSucesso.setText("");
            lblErro.setText("Erro ao salvar dados do usuário.");
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
