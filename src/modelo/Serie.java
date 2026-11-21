package modelo;

/**
 * Série: uma mídia dividida em temporadas e episódios.
 */
public class Serie extends Midia implements Exibivel {
    /** Quantidade de temporadas. */
    private final int temporadas;
    /** Episódios em cada temporada (o mesmo número para todas). */
    private final int episodiosPorTemporada;
    /** Duração média de um episódio, em minutos. */
    private final int duracaoMediaEpisodio;

    /**
     * Cria uma série.
     *
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

    /**
     * Informa quantos episódios há em cada temporada.
     *
     * @return quantidade de episódios de cada temporada
     */
    public int getEpisodiosPorTemporada() {
        return episodiosPorTemporada;
    }

    /**
     * Informa a duração média de um episódio.
     *
     * @return duração média de um episódio, em minutos
     */
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

    /**
     * Informa quantas temporadas a série tem.
     *
     * @return quantidade de temporadas
     */
    public int getTemporadas() {
        return temporadas;
    }
}