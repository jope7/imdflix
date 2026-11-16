package excecoes;

/*
    lançada quando tenta cadastrar um username que já esta em uso
    por jope
*/
public class UsernameJaExisteException extends Exception {    
    public UsernameJaExisteException(String user) {
        super("O username '" + user + "' ja esta em uso.");
    }
}