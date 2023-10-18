package br.com.desafiovr.miniautorizador.enums;

import lombok.Getter;

@Getter
public enum RegrasAutorizacaoTransacao {
    SALDO_INSUFICIENTE,
    SENHA_INVALIDA,
    CARTAO_INEXISTENTE
}
