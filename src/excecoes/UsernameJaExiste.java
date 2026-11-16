package excecoes;

/*
    lançada quando tenta cadastrar um username que já esta em uso
    por jope
*/
public class UsernameJaExiste extends Exception {    
    public UsernameJaExiste(String user) {
        super("O username '" + user + "' ja esta em uso.");
    }
}