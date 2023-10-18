package br.com.desafiovr.miniautorizador.exceptions;

public class CartaoNotFoundException extends Exception {

    public CartaoNotFoundException(String mensagem) {
        super(mensagem);
    }

}
