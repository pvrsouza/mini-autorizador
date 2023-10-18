package br.com.desafiovr.miniautorizador.service.transacoes.processador;

import br.com.desafiovr.miniautorizador.enums.RegrasAutorizacaoTransacao;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.exceptions.SenhaInvalidaException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import br.com.desafiovr.miniautorizador.service.CartaoService;
import br.com.desafiovr.miniautorizador.service.MensagensService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ValidaSenhaTransacao implements ValidacaoTransacao {

    private final MensagensService mensagensService;

    private final CartaoService cartaoService;

    public ValidaSenhaTransacao(MensagensService mensagensService, CartaoService cartaoService) {
        this.mensagensService = mensagensService;
        this.cartaoService = cartaoService;
    }

    @Override
    public void validar(TransacaoRequestDto request) throws ValidacaoTransacaoException {
        String numeroCartao = request.getNumeroCartao();
        log.info("Validando senha para o cartão informado {}", numeroCartao);

        try {
            this.cartaoService.validaSenha(numeroCartao, request.getSenhaCartao());
        } catch (SenhaInvalidaException e) {
            String errorMessage = this.mensagensService.getErrorMessage("error.cartao.senha.invalida");
            log.info(errorMessage);
            throw new ValidacaoTransacaoException(errorMessage, RegrasAutorizacaoTransacao.SENHA_INVALIDA);
        }

        log.info("Senha validada para o cartão informado {}", numeroCartao);
    }
}
