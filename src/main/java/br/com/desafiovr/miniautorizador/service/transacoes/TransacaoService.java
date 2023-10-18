package br.com.desafiovr.miniautorizador.service.transacoes;

import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;

public interface TransacaoService {
    void registra(TransacaoRequestDto transacaoRequestDto) throws ValidacaoTransacaoException, CartaoNotFoundException;
}
