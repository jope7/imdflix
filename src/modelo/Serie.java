package modelo;

public class Serie extends Midia implements Exibivel {
    private final int temporadas;
    private final int episodiosPorTemporada;
    private final int duracaoMediaEpisodio;

    public Serie(String titulo, String genero, int anoLancamento, int temporadas,  int episodiosPorTemporada, 
                int duracaoMediaEpisodio) {
        super(titulo, genero, anoLancamento);
        
        if (temporadas <= 0 || episodiosPorTemporada <= 0 || duracaoMediaEpisodio <= 0) {
            throw new IllegalArgumentException("Temporadas, episodios e duracao devem ser positivas");
        }

        this.temporadas = temporadas;
        this.episodiosPorTemporada = episodiosPorTemporada;
        this.duracaoMediaEpisodio = duracaoMediaEpisodio;
    }

    @Override
    public String getTipo() {
        return "Serie";
    }

    @Override
    public int getDuracaoTotalMinutos() {
        return temporadas * episodiosPorTemporada + duracaoMediaEpisodio;
    }

    @Override
    public String getResumoExibicao() {
        return temporadas + " temporada(s), " + episodiosPorTemporada + " episodios cada";
    }

    public int getTemporadas() {
        return temporadas;
    }
}