package excecoes;

import java.io.IOException;

/*
    quando um arquivo de dados (JSON) está malformado ou contém valores inválidos
*/
public class DadosInvalidosException extends IOException {
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }

    public DadosInvalidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
