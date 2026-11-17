package modelo;

import excecoes.NotaInvalidaException;

/**
 * Classe base de tudo que existe no catálogo (filme, série, documentário).
 * Guarda os dados comuns e acumula as notas recebidas para calcular a média.
 */
public abstract class Midia implements Avaliavel {
    private String titulo;
    private String genero;
    private int anoLancamento;
    private double somaNotas;
    private int quantidadeNotas;

    /**
     * @param titulo título (não pode ser vazio)
     * @param genero gênero (não pode ser vazio)
     * @param anoLancamento ano de lançamento (a partir de 1888)
     * @throws IllegalArgumentException se algum dado for inválido
     */
    protected Midia(String titulo, String genero, int anoLancamento) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O titulo nao pode ser vazio.");
        }

        if (genero == null || genero.isBlank()) {
            throw new IllegalArgumentException("O genero nao pode ser vazio.");
        }

        if (anoLancamento < 1888) {
            throw new IllegalArgumentException("Ano de lancamento invalido: " + anoLancamento);
        }

        this.titulo = titulo;
        this.genero = genero;
        this.anoLancamento = anoLancamento;

    }

    /** @return o tipo da mídia (ex.: "Filme", "Serie", "Documentario") */
    public abstract String getTipo();

    /**
     * Valida que a nota está entre 0 e 10.
     *
     * @param nota nota a validar
     * @throws NotaInvalidaException se estiver fora do intervalo
     */
    public static void validarNota(double nota) throws NotaInvalidaException {
        if (!(nota >= 0 && nota <= 10)) {
            throw new NotaInvalidaException(nota);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void avaliar(double nota) throws NotaInvalidaException {
        validarNota(nota);
        somaNotas += nota;
        quantidadeNotas++;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException se a mídia não tem nenhuma avaliação
     */
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

    /** {@inheritDoc} */
    @Override
    public void substituirAvaliacao(double notaAntiga, double notaNova) throws NotaInvalidaException {
        validarNota(notaNova);
        removerAvaliacao(notaAntiga);
        somaNotas+=notaNova;
        quantidadeNotas++;  // add pq removerAvaliacao eh chamado antes
    }

    /** {@inheritDoc} Sem avaliações, a média é 0. */
    @Override
    public double getNotaMedia() {
        return quantidadeNotas == 0 ? 0.0 : somaNotas / quantidadeNotas;
    }

    /** {@inheritDoc} */
    @Override
    public int getQuantidadeAvaliacoes() {
        return quantidadeNotas;
    }

    /** @return o título */
    public String getTitulo() {
        return titulo;
    }

    /** @return o gênero */
    public String getGenero() {
        return genero;
    }

    /** @return o ano de lançamento */
    public int getAnoLancamento() {
        return anoLancamento;
    }

    @Override
    public String toString() {
        return getTipo() + ": " + titulo + " (" + anoLancamento + ", " + genero + ")";
    }
}
