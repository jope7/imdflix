package modelo;

import excecoes.NotaInvalidaException;

/*
    para tudo que pode receber avaliacoes dos usuarios
*/
public interface Avaliavel {
    void avaliar(double nota) throws NotaInvalidaException;

    double getNotaMedia();

    int getQuantidadeAvaliacoes();
}