package br.com.desafiovr.miniautorizador.service.transacoes.processador;

import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;

public interface ValidacaoTransacao {
    void validar(TransacaoRequestDto request) throws ValidacaoTransacaoException;
}
