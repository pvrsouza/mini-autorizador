package br.com.desafiovr.miniautorizador.exceptions;

public class SenhaInvalidaException extends RuntimeException {

    public SenhaInvalidaException(String mensagem) {
        super(mensagem);
    }

}
