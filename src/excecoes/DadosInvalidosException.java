package excecoes;

import java.io.IOException;

/**
 * Lançada quando um arquivo de dados (JSON) está malformado ou contém valores inválidos.
 *
 * Herda de {@link IOException} para caber nas assinaturas dos repositórios, que já declaram
 * {@code throws IOException}. A mensagem indica o arquivo, a posição do item e o motivo do erro.
 */
public class DadosInvalidosException extends IOException {
    /**
     * Cria a exceção com uma mensagem descrevendo o problema.
     *
     * @param mensagem descrição do problema encontrado nos dados
     */
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria a exceção com uma mensagem e a causa original.
     *
     * @param mensagem descrição do problema encontrado nos dados
     * @param causa a exceção que originou o problema (por exemplo, um erro do leitor de JSON)
     */
    public DadosInvalidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
