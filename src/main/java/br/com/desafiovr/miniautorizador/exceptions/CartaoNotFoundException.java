package br.com.desafiovr.miniautorizador.exceptions;

import lombok.Getter;

public class CartaoNotFoundException extends Exception {

    public CartaoNotFoundException(String mensagem) {
        super(mensagem);
    }

}
