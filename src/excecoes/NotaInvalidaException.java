package excecoes;

/*
    lançada quando uma nota fora do intervalo permitido é inserida
*/
public class NotaInvalidaException extends Exception {    
    public NotaInvalidaException(double nota) {
        super("Nota invvalida: " + nota + ". A nota deve estar entre 0 e 10.");
    }

}