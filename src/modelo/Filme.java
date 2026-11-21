package modelo;

/**
 * Filme: uma mídia com duração única e um diretor.
 */
public class Filme extends Midia implements Exibivel {
    /** Duração do filme, em minutos. */
    private final int duracaoMinutos;
    /** Nome do diretor. */
    private final String diretor;

    /**
     * Cria um filme.
     *
     * @param titulo título (não pode ser vazio)
     * @param genero gênero (não pode ser vazio)
     * @param anoLancamento ano de lançamento (a partir de 1888)
     * @param duracaoMinutos duração em minutos (positiva)
     * @param diretor nome do diretor (não pode ser vazio)
     * @throws IllegalArgumentException se algum dado for inválido
     */
    public Filme(String titulo, String genero, int anoLancamento, int duracaoMinutos, String diretor) {
        super(titulo, genero, anoLancamento);

        if (duracaoMinutos <= 0) {
            throw new IllegalArgumentException("A duracao deve ser positiva.");
        }

        if (diretor == null || diretor.isBlank()) {
           throw new IllegalArgumentException("O diretor nao pode ser vazio.");
        }

        this.duracaoMinutos = duracaoMinutos;
        this.diretor = diretor;
    }

    @Override
    public String getTipo() {
        return "Filme";
    }

    @Override
    public int getDuracaoTotalMinutos() {
        return duracaoMinutos;
    }

    @Override
    public String getResumoExibicao() {
        return duracaoMinutos + " min, dirigido por " + diretor;
    }

    /**
     * Informa quem dirigiu o filme.
     *
     * @return o nome do diretor
     */
    public String getDiretor() {
        return diretor;
    }
}
