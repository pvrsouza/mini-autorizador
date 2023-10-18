package br.com.desafiovr.miniautorizador.service.transacoes.processador;

import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;

public interface CadeiaValidacoesTransacao {

    void execute(TransacaoRequestDto request);
}
