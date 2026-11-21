package excecoes;

/**
 * Lançada ao tentar cadastrar um username que já está em uso (sem diferenciar maiúsculas de minúsculas).
 *
 * @author jope
 */
public class UsernameJaExisteException extends Exception {    
    /**
     * Cria a exceção com uma mensagem que cita o username repetido.
     *
     * @param user o username que já está em uso
     */
    public UsernameJaExisteException(String user) {
        super("O username '" + user + "' ja esta em uso.");
    }
}