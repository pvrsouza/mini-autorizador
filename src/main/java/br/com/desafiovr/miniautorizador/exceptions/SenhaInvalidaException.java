package br.com.desafiovr.miniautorizador.exceptions;

import lombok.Getter;

public class SenhaInvalidaException extends RuntimeException {

    public SenhaInvalidaException(String mensagem) {
        super(mensagem);
    }

}
