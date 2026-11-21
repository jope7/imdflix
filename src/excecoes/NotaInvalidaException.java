package excecoes;

/**
 * Lançada quando uma nota fora do intervalo permitido (0 a 10) é informada.
 *
 * É uma exceção verificada: quem avalia um título precisa tratá-la, por exemplo mostrando uma mensagem na tela.
 */
public class NotaInvalidaException extends Exception {    
    /**
     * Cria a exceção com uma mensagem que cita a nota recebida.
     *
     * @param nota a nota inválida que foi informada
     */
    public NotaInvalidaException(double nota) {
        super("Nota inválida: " + nota + ". A nota deve estar entre 0 e 10.");
    }

}
