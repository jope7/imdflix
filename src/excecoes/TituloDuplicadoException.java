package excecoes;

/*
    lançada quando tenta cadastrar um titulo que já existe no catalogo
    por jope
*/
public class TituloDuplicadoException extends Exception {    
    public TituloDuplicadoException(String titulo) {
        super("O titulo '" + titulo + "' ja existe no catalogo");
    }
}