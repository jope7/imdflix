package modelo;

public class Filme extends Midia implements Exibivel {
    private final int duracaoMinutos;
    private final String diretor;

    public Filme(String titulo, String genero, int anoLancamento, int duracaoMinutos, String diretor) {
        super(titulo, genero, anoLancamento);

        if (duracaoMinutos <= 0) {
            throw new IllegalArgumentException("A duracao deve ser positiva.");
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

    public String getDiretor() {
        return diretor;
    }
}
