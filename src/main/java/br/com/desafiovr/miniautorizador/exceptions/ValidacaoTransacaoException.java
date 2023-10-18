package br.com.desafiovr.miniautorizador.exceptions;

import br.com.desafiovr.miniautorizador.enums.RegrasAutorizacaoTransacao;
import lombok.Getter;

@Getter
public class ValidacaoTransacaoException extends RuntimeException {

    private RegrasAutorizacaoTransacao regrasAutorizacaoTransacao;

    public ValidacaoTransacaoException(String message, RegrasAutorizacaoTransacao regrasAutorizacaoTransacao) {
        super(message);
        this.regrasAutorizacaoTransacao = regrasAutorizacaoTransacao;
    }

}
