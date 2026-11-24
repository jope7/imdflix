package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import modelo.Midia;

/**
 * Cartão visual de um título: capa colorida com a inicial e o tipo, e embaixo o nome, o gênero com
 * o ano e a nota média. É usado pelas telas que mostram títulos (catálogo, minha lista e
 * recomendações), para todas terem a mesma aparência sem repetir código.
 */
final class CartaoMidia {
    /** Largura do cartão, em pixels. */
    static final double LARGURA = 150;
    /** Altura da capa, em pixels. */
    private static final double ALTURA_CAPA = 200;
    /** Cores das capas, escolhidas pelo gênero (apenas aparência). */
    private static final String[] CORES = {
        "#b81d24", "#1f6feb", "#8957e5", "#2ea043", "#d29922", "#db6d28", "#1b7c83", "#bf3989"
    };

    private CartaoMidia() {
    }

    /**
     * Monta o cartão de um título.
     *
     * @param midia o título
     * @param aoClicar o que fazer quando o cartão for clicado (por exemplo, abrir o detalhe)
     * @return o cartão
     */
    static VBox criar(Midia midia, Runnable aoClicar) {
        Label inicial = new Label(midia.getTitulo().substring(0, 1).toUpperCase());
        inicial.getStyleClass().add("capa-letra");

        Label tipo = new Label(midia.getTipo().toUpperCase());
        tipo.getStyleClass().add("capa-tipo");

        StackPane capa = new StackPane(inicial, tipo);
        StackPane.setAlignment(tipo, Pos.TOP_LEFT);
        StackPane.setMargin(tipo, new Insets(8));
        capa.getStyleClass().add("capa");
        capa.setStyle("-fx-background-color: linear-gradient(to bottom right, "
                + corDoGenero(midia.getGenero()) + ", #141414);");
        capa.setMinSize(LARGURA, ALTURA_CAPA);
        capa.setPrefSize(LARGURA, ALTURA_CAPA);
        capa.setMaxSize(LARGURA, ALTURA_CAPA);

        Label nome = new Label(midia.getTitulo());
        nome.getStyleClass().add("card-titulo");
        nome.setMaxWidth(LARGURA);

        Label info = new Label(midia.getGenero() + " · " + midia.getAnoLancamento());
        info.getStyleClass().add("card-info");
        info.setMaxWidth(LARGURA);

        String textoNota = midia.getQuantidadeAvaliacoes() == 0
                ? "Sem avaliações"
                : String.format("★ %.1f", midia.getNotaMedia());
        Label nota = new Label(textoNota);
        nota.getStyleClass().add("card-nota");

        VBox cartao = new VBox(6, capa, nome, info, nota);
        cartao.getStyleClass().add("card");
        cartao.setPrefWidth(LARGURA);
        cartao.setMaxWidth(LARGURA);
        cartao.setOnMouseClicked(e -> aoClicar.run());
        return cartao;
    }

    /**
     * Escolhe a cor da capa a partir do gênero, para títulos do mesmo gênero terem a mesma cor.
     *
     * @param genero o gênero do título
     * @return a cor em hexadecimal
     */
    private static String corDoGenero(String genero) {
        return CORES[Math.floorMod(genero.toLowerCase().hashCode(), CORES.length)];
    }
}