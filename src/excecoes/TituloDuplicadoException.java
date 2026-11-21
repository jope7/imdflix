package excecoes;

/**
 * Lançada ao tentar cadastrar um título que já existe no catálogo (sem diferenciar maiúsculas de minúsculas).
 *
 * @author jope
 */
public class TituloDuplicadoException extends Exception {    
    /**
     * Cria a exceção com uma mensagem que cita o título repetido.
     *
     * @param titulo o título que já existe no catálogo
     */
    public TituloDuplicadoException(String titulo) {
        super("O titulo '" + titulo + "' ja existe no catalogo");
    }
}