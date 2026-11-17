package modelo;

/**
 * Série: uma mídia dividida em temporadas e episódios.
 */
public class Serie extends Midia implements Exibivel {
    private final int temporadas;
    private final int episodiosPorTemporada;
    private final int duracaoMediaEpisodio;

    /**
     * @param titulo título (não pode ser vazio)
     * @param genero gênero (não pode ser vazio)
     * @param anoLancamento ano de lançamento (a partir de 1888)
     * @param temporadas quantidade de temporadas (positiva)
     * @param episodiosPorTemporada episódios em cada temporada (positivo)
     * @param duracaoMediaEpisodio duração média de um episódio, em minutos (positiva)
     * @throws IllegalArgumentException se algum dado for inválido
     */
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

    /** @return quantidade de episódios de cada temporada */
    public int getEpisodiosPorTemporada() {
        return episodiosPorTemporada;
    }

    /** @return duração média de um episódio, em minutos */
    public int getDuracaoMediaEpisodio() {
        return duracaoMediaEpisodio;
    }

    /** @return duração de todos os episódios somados (temporadas x episódios x duração média) */
    @Override
    public int getDuracaoTotalMinutos() {
        return temporadas * episodiosPorTemporada * duracaoMediaEpisodio;
    }

    @Override
    public String getResumoExibicao() {
        return temporadas + " temporada(s), " + episodiosPorTemporada + " episodios cada";
    }

    /** @return quantidade de temporadas */
    public int getTemporadas() {
        return temporadas;
    }
}