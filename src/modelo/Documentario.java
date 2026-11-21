package modelo;

/**
 * Documentário: uma mídia com duração única e um tema.
 */
public class Documentario extends Midia implements Exibivel {
    /** Duração do documentário, em minutos. */
    private final int duracaoMinutos;
    /** Tema abordado. */
    private final String tema;

    /**
     * Cria um documentário.
     *
     * @param titulo título (não pode ser vazio)
     * @param genero gênero (não pode ser vazio)
     * @param anoLancamento ano de lançamento (a partir de 1888)
     * @param duracaoMinutos duração em minutos (positiva)
     * @param tema tema abordado (não pode ser vazio)
     * @throws IllegalArgumentException se algum dado for inválido
     */
    public Documentario(String titulo, String genero, int anoLancamento, int duracaoMinutos, String tema) {
        super(titulo, genero, anoLancamento);
        
        if (duracaoMinutos <= 0) {
            throw new IllegalArgumentException("A duracao deve ser positiva.");
        }

        if (tema == null || tema.isBlank()) {
            throw new IllegalArgumentException("O tema nao pode ser vazio.");
        }

        this.duracaoMinutos = duracaoMinutos;
        this.tema = tema;
    }
    
    @Override
    public String getTipo() {
        return "Documentario";
    }

    @Override
    public int getDuracaoTotalMinutos() {
        return duracaoMinutos;
    }

    @Override
    public String getResumoExibicao() {
        return duracaoMinutos + " min sobre " + tema;
    }

    /**
     * Informa o assunto tratado no documentário.
     *
     * @return o tema abordado
     */
    public String getTema() {
        return tema;
    }

}
