package test;

import excecoes.NotaInvalidaException;
import modelo.Filme;
import modelo.Midia;
import modelo.Usuario;

public class TesteUsuario {
    
    public static void main(String[] args) throws Exception{
        testaUsernameNormalizado();
        testaUsernameInvalido();
        testaMesmoNomeUsernamesDiferentes();
        testaListaSemDuplicados();
        testaAvaliarEReavaliar();
        testaReavaliarComNotaInvalidaPreservaAnterior();
        testaDoisUsuariosAvaliandoMesmaMidia();
        System.out.println("todos os testes passaram.");
    }

    static Midia novoFilme() {
        return new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski");
    }

    static void testaUsernameNormalizado() {
        Usuario u = new Usuario("  Joack_Dev ", "Joackym");
        assert u.getUsername().equals("joack_dev") : "Username deveria virar minusculo e sem espacos";
    }

    static void testaUsernameInvalido() {
        String[] invalidos = {"ab", "com espaco", "nome-com-hifen", "x".repeat(21), ""};
        for (String invalido : invalidos) {
            boolean lancou = false;
            try {
                new Usuario(invalido, "Fulano");
            } catch (IllegalArgumentException e) {
                lancou = true;
            }
            assert lancou : "Username deveria ser rejeitado: '" + invalido + "'";
        }
    }

    static void testaMesmoNomeUsernamesDiferentes() {
        Usuario a = new Usuario("76ope", "Joackym");
        Usuario b = new Usuario("joack_dev", "Joackym");
        assert !a.equals(b) : "Usernames diferentes sao usuarios diferentes";
        assert a.equals(new Usuario("76OPE", "Outro Nome")) : "Mesmo username (sem diferenciar caixa) e o mesmo usuario";
    }

    static void testaListaSemDuplicados() {
        Usuario u = new Usuario("ana01", "Ana");
        Midia m = novoFilme();
        u.adicionarNaLista(m);
        u.adicionarNaLista(m);
        assert u.getMinhaLista().size() == 1;
        u.removerDaLista(m);
        assert u.getMinhaLista().isEmpty();
    }

    static void testaAvaliarEReavaliar() throws NotaInvalidaException {
        Usuario u = new Usuario("ana01", "Ana");
        Midia m = novoFilme();
        u.avaliar(m, 8);
        u.avaliar(m, 6);
        assert m.getQuantidadeAvaliacoes() == 1 : "Reavaliar nao deve contar duas vezes";
        assert m.getNotaMedia() == 6.0;
        assert u.getAvaliacoes().get("Matrix") == 6.0;
    }

    static void testaReavaliarComNotaInvalidaPreservaAnterior() throws NotaInvalidaException {
        Usuario u = new Usuario("ana01", "Ana");
        Midia m = novoFilme();
        u.avaliar(m, 8);
        boolean lancou = false;
        try {
            u.avaliar(m, -1);
        } catch (NotaInvalidaException e) {
            lancou = true;
        }
        assert lancou;
        assert m.getNotaMedia() == 8.0 && u.getAvaliacoes().get("Matrix") == 8.0 : "A nota anterior deveria ser preservada";
    }

    static void testaDoisUsuariosAvaliandoMesmaMidia() throws NotaInvalidaException {
        Usuario ana = new Usuario("ana01", "Ana");
        Usuario beto = new Usuario("beto02", "Beto");
        Midia m = novoFilme();
        ana.avaliar(m, 10);
        beto.avaliar(m, 6);
        assert m.getQuantidadeAvaliacoes() == 2 && m.getNotaMedia() == 8.0;
        beto.avaliar(m, 8);
        assert m.getNotaMedia() == 9.0 : "A media deveria considerar a nova nota do Beto";
    }
}