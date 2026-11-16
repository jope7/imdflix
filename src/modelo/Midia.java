package modelo;

import excecoes.NotaInvalidaException;

/*
    classe abstrata base para todo catalogo
*/
public abstract class Midia implements Avaliavel {
    private String titulo;
    private String genero;
    private int anoLancamento;
    private double somaNotas;
    private int quantidadeNotas;

    protected Midia(String titulo, String genero, int anoLancamento) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O titulo nao pode ser vazio.");
        }

        if (genero == null || genero.isBlank()) {
            throw new IllegalArgumentException("O genero nao pode ser vazio.");
        }

        if (anoLancamento < 1888) {
            throw new IllegalArgumentException("Ano de lançamento invalido: " + anoLancamento);
        }

        this.titulo = titulo;
        this.genero = genero;
        this.anoLancamento = anoLancamento;

    }

    // cada classe informa seu tipo (filme, serie, doc)
    public abstract String getTipo();

    private static void validarNota(double nota) throws NotaInvalidaException {
        if (nota < 0 || nota > 10) {
            throw new NotaInvalidaException(nota);
        }
    }

    @Override
    public void avaliar(double nota) throws NotaInvalidaException {
        validarNota(nota);
        somaNotas += nota;
        quantidadeNotas++;
    }

    @Override
    public void removerAvaliacao(double nota) {
        if (quantidadeNotas == 0) {
            throw new IllegalArgumentException("Nao ha avaliacoes para remover.");
        }

        somaNotas -= nota;
        quantidadeNotas--;
        if (quantidadeNotas == 0) {
            somaNotas=0;
        }
    }

    @Override
    public void substituirAvaliacao(double notaAntiga, double notaNova) throws NotaInvalidaException {
        validarNota(notaNova);
        removerAvaliacao(notaAntiga);
        somaNotas=+notaNova;
        quantidadeNotas++;  // add pq removerAvaliacao eh chamado antes
    }

    @Override
    public double getNotaMedia() {
        return quantidadeNotas == 0 ? 0.0 : somaNotas / quantidadeNotas;
    }

    @Override
    public int getQuantidadeAvaliacoes() {
        return quantidadeNotas;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getGenero() {
        return genero;
    }

    public int getAnoLancamento() {
        return anoLancamento;
    }

    @Override
    public String toString() {
        return getTipo() + ": " + titulo + " (" + anoLancamento + ", " + genero + ")";
    }
}
