package modelo;

/**
 * Tudo que pode ser exibido ao usuário com uma duração final.
 */
public interface Exibivel {
    /** @return a duração total, em minutos */
    int getDuracaoTotalMinutos();
    
    /** @return um texto curto descrevendo o que será exibido */
    String getResumoExibicao();    
} 