package test;

import excecoes.NotaInvalidaException;
import modelo.Filme;
import modelo.Midia;
import modelo.Usuario;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TesteUsuario {
    
    public static void main(String[] args) throws Exception{
        testaUsernameNormalizado();
        testaUsernameInvalido();
        testaMesmoNomeUsernamesDiferentes();
        testaListaSemDuplicados();
        testaAvaliarEReavaliar();
        testaReavaliarComNotaInvalidaPreservaAnterior();
        testaDoisUsuariosAvaliandoMesmaMidia();
        testaRestaurarPreencheListaEAvaliacoes();
        testaRestaurarNaoAlteraMidia();
        testaRestaurarIgnoraTitulosDuplicados();
        testaRestaurarAceitaNulos();
        testaRestaurarRejeitaDadosInvalidos();
        testaReavaliarUsuarioRestauradoDepoisDeAplicarNotas();
        testaRemoverDaListaDeMidiaAusente();
        System.out.println("✅ TesteUsuario - todos os testes passaram.");
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

    static void testaRestaurarPreencheListaEAvaliacoes() throws Exception {
        Usuario u = Usuario.restaurar("  ANA01 ", "Ana", List.of("Matrix", "Dark"), Map.of("Matrix", 9.0));
        assert u.getUsername().equals("ana01");
        assert u.getNome().equals("Ana");
        assert u.getMinhaLista().equals(List.of("Matrix", "Dark"));
        assert u.getAvaliacoes().get("Matrix") == 9.0;
    }

    static void testaRestaurarNaoAlteraMidia() throws Exception {
        Midia m = novoFilme();
        Usuario.restaurar("ana01", "Ana", List.of("Matrix"), Map.of("Matrix", 9.0));
        assert m.getQuantidadeAvaliacoes() == 0 && m.getNotaMedia() == 0.0 : "Restaurar nao pode mexer em nenhuma midia";
    }

    static void testaRestaurarIgnoraTitulosDuplicados() throws Exception {
        Usuario u = Usuario.restaurar("ana01", "Ana", List.of("Matrix", "Matrix"), null);
        assert u.getMinhaLista().size() == 1;
    }

    static void testaRestaurarAceitaNulos() throws Exception {
        Usuario u = Usuario.restaurar("ana01", "Ana", null, null);
        assert u.getMinhaLista().isEmpty() && u.getAvaliacoes().isEmpty();
    }

    static void testaRestaurarRejeitaDadosInvalidos() {
        boolean notaInvalida = false;
        try {
            Usuario.restaurar("ana01", "Ana", null, Map.of("Matrix", 11.0));
        } catch (NotaInvalidaException e) {
            notaInvalida = true;
        }
        assert notaInvalida : "Nota fora de 0..10 deveria ser rejeitada";

        Map<String, Double> notaNula = new LinkedHashMap<>();
        notaNula.put("Matrix", null);
        boolean lancou = false;
        try {
            Usuario.restaurar("ana01", "Ana", null, notaNula);
        } catch (IllegalArgumentException | NotaInvalidaException e) {
            lancou = true;
        }
        assert lancou : "Nota nula deveria ser rejeitada";

        lancou = false;
        try {
            Usuario.restaurar("ab", "Ana", null, null);
        } catch (IllegalArgumentException | NotaInvalidaException e) {
            lancou = true;
        }
        assert lancou : "Username invalido deveria ser rejeitado";

        lancou = false;
        try {
            Usuario.restaurar("ana01", "Ana", List.of(" "), null);
        } catch (IllegalArgumentException | NotaInvalidaException e) {
            lancou = true;
        }
        assert lancou : "Titulo vazio na lista deveria ser rejeitado";
    }

    // Usuario restaurado + notas reaplicadas na midia: reavaliar continua substituindo
    static void testaReavaliarUsuarioRestauradoDepoisDeAplicarNotas() throws Exception {
        Midia m = novoFilme();
        Usuario u = Usuario.restaurar("ana01", "Ana", null, Map.of("Matrix", 8.0));
        m.avaliar(8.0); // o que Catalogo.aplicarAvaliacoes faz ao iniciar
        u.avaliar(m, 4);
        assert m.getQuantidadeAvaliacoes() == 1 && m.getNotaMedia() == 4.0;
    }

    static void testaRemoverDaListaDeMidiaAusente() {
        Usuario u = new Usuario("ana01", "Ana");
        u.removerDaLista(novoFilme());
        assert u.getMinhaLista().isEmpty();
    }
}
