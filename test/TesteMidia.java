package test;

import java.util.List;

import excecoes.NotaInvalidaException;
import modelo.Documentario;
import modelo.Exibivel;
import modelo.Filme;
import modelo.Midia;
import modelo.Serie;


/*
    teste com assert
    executar com -ea 
*/
public class TesteMidia {
    
    public static void main(String[] args) throws Exception {
        testaNotaMedia();
        testaNotaInvalida();
        testaTituloVazio();
        testaPolimorfismo();
        System.out.println("Todos os testes passaram.");
    }

    static void testaNotaMedia() throws NotaInvalidaException {
        Midia m = new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski");
        
        m.avaliar(8);
        
        m.avaliar(10);
        
        assert m.getNotaMedia() == 9.0 : "A media deveria ser 9.0";
        
        assert m.getQuantidadeAvaliacoes() == 2;
    }

    static void testaNotaInvalida() {
        Midia m = new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski");
        boolean lancou = false;
        
        try {
            m.avaliar(11);
        } catch (NotaInvalidaException e) {
            lancou = true;
        }
        
        assert lancou : "Deveria lancar NotaInvalidaException";
    }

    static void testaTituloVazio() {
        boolean lancou = false;
        
        try {
            new Filme(" ", "Drama", 2000, 90, "X");
        } catch (IllegalArgumentException e) {
            lancou = true;
        }
        
        assert lancou : "Titulo vazio deveria ser rejeitado";
    }

    static void testaPolimorfismo() {
        
        List<Midia> catalogo = List.of(
            new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski"),
            new Serie("Dark", "Suspense", 2017, 3, 8, 50),
            new Documentario("Free Solo", "Aventura", 2018, 100, "Escalada"));
        
            int total = 0;
        
        for (Midia m : catalogo) {
            total += ((Exibivel) m).getDuracaoTotalMinutos();
        }
        
        assert total == 136 + 1200 + 100 : "Soma das duracoes incorreta: " + total;
    }
}
