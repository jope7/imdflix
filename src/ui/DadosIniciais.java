package ui;

import excecoes.TituloDuplicadoException;
import excecoes.UsernameJaExisteException;
import modelo.Documentario;
import modelo.Filme;
import modelo.Serie;
import servico.Catalogo;
import servico.GerenciadorUsuarios;

/*
    dados fixos para testar as telas enquanto a persistencia nao fica pronta
*/
public class DadosIniciais {

    public static void carregar(Catalogo catalogo, GerenciadorUsuarios gerenciador)
            throws TituloDuplicadoException, UsernameJaExisteException {
        catalogo.cadastrar(new Filme("Matrix", "Ficção", 1999, 136, "Wachowski"));
        catalogo.cadastrar(new Filme("Interestelar", "Ficção", 2014, 169, "Christopher Nolan"));
        catalogo.cadastrar(new Serie("Dark", "Suspense", 2017, 3, 8, 50));
        catalogo.cadastrar(new Serie("Breaking Bad", "Drama", 2008, 5, 13, 47));
        catalogo.cadastrar(new Documentario("Free Solo", "Aventura", 2018, 100, "Escalada"));

        gerenciador.cadastrar("teste", "Usuário de Teste");
    }
}
