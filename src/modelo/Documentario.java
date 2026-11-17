package modelo;

public class Documentario extends Midia implements Exibivel {
    private final int duracaoMinutos;
    private final String tema;

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

    public String getTema() {
        return tema;
    }

}
