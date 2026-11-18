package modelo;

import excecoes.NotaInvalidaException;

/**
 * Tudo que pode receber avaliações dos usuários.
 */
public interface Avaliavel {
    /**
     * Registra uma nova avaliação.
     *
     * @param nota nota de 0 a 10
     * @throws NotaInvalidaException se a nota estiver fora do intervalo
     */
    void avaliar(double nota) throws NotaInvalidaException;

    /**
     * Desfaz uma avaliação já registrada.
     *
     * @param nota a nota que foi dada
     */
    void removerAvaliacao(double nota);

    /**
     * Troca uma avaliação por outra, sem alterar a quantidade de avaliações.
     *
     * @param notaAntiga nota que havia sido dada
     * @param notaNova nota que passa a valer
     * @throws NotaInvalidaException se a nota nova estiver fora do intervalo
     */
    void substituirAvaliacao(double notaAntiga, double notaNova) throws NotaInvalidaException;

    /** @return a média das notas (0 se não há avaliações) */
    double getNotaMedia();

    /** @return quantas avaliações foram registradas */
    int getQuantidadeAvaliacoes();
}