package test;

import excecoes.NotaInvalidaException;
import modelo.Filme;
import modelo.Usuario;

public class TesteExcecoes {

    public static void main(String[] args) throws Exception {
        testaNotaForaDoIntervalo();
        testaNotasNosLimites();
        testaNotaNaN();
        testaUsernameInvalido();
        testaNomeVazio();
        testaReavaliacaoSubstituiNota();
        System.out.println("✅ TesteExcecoes - todos os testes passaram.");
    }

    static void testaNotaForaDoIntervalo() {
        Filme f = new Filme("Matrix", "Ficcao", 1999, 136, "Wachowski");

        for (double nota : new double[] {-1, 11}) {
            boolean lancou = false;
            try {
                f.avaliar(nota);
            } catch (NotaInvalidaException e) {
                lancou = true;
            }
            assert lancou : "Nota " + nota + " deveria lancar NotaInvalidaException";
        }
        assert f.getQuantidadeAvaliacoes() == 0 : "Nota invalida nao pode ser contada";
    }

    static void testaNotasNosLimites() throws Exception {
        Filme f = new Filme("Matrix", "Ficção", 1999, 136, "Wachowski");

        f.avaliar(0); //Limite inferior
        f.avaliar(10); //Limite superior

        assert f.getQuantidadeAvaliacoes() == 2 : "Notas 0 e 10 deveriam ser aceitas";
        assert f. getNotaMedia() == 5.0 : "Media de 0 a 10 deveria ser 5.0";
    }

    static void testaNotaNaN() {
        Filme f = new Filme("Matrix", "Ficção", 1999, 136, "Wachowski");

        boolean lancou = false;
        try {
            f.avaliar(Double.NaN);
        } catch (NotaInvalidaException e) {
            lancou = true;
        }
        assert lancou : "Nota NaN deveria lançar NotaInvalidaException";
        assert f.getQuantidadeAvaliacoes() == 0 : "Nota NaN nao pode ser contada";
    }

    static void testaUsernameInvalido() {
        for (String username : new String[] {"ab", "com espaço", null}) {
            boolean lancou = false;
            try {
                new Usuario(username, "Teste");
            } catch (IllegalArgumentException e) {
                lancou = true;
            }
            assert lancou : "Username '" + username + "' deveria lancar IllegalArgumentException";
        }
    }

    static void testaNomeVazio() {
        boolean lancou = false;
        try {
            new Usuario("vitor", "    ");
        } catch (IllegalArgumentException e){
            lancou = true;
        }
        assert lancou : "Nome vazio deveria lancar IllegalArgumentException";
    }

    static void testaReavaliacaoSubstituiNota() throws Exception {
        Filme f = new Filme("Matrix", "Ficção", 1999, 136, "Wachowski");

        Usuario u = new Usuario("trembo", "Luiz");

        u.avaliar(f, 4);
        u.avaliar(f, 8);

        assert f. getQuantidadeAvaliacoes() == 1; //A segunda substitui a primeira e não soma com ela
        assert f. getNotaMedia() == 8.0; //só a nota 8 conta
        assert u. getAvaliacoes().get("Matrix") == 8.0; //o usuário guardou a nota nova
    }
}
