package br.com.desafiovr.miniautorizador.service.transacoes.processador;

import br.com.desafiovr.miniautorizador.enums.RegrasAutorizacaoTransacao;
import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import br.com.desafiovr.miniautorizador.service.CartaoService;
import br.com.desafiovr.miniautorizador.service.MensagensService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ValidaCartaoTransacao implements ValidacaoTransacao {

    private final CartaoService cartaoService;

    private final MensagensService mensagensService;

    public ValidaCartaoTransacao(CartaoService cartaoService, MensagensService mensagensService) {
        this.cartaoService = cartaoService;
        this.mensagensService = mensagensService;
    }

    @Override
    public void validar(TransacaoRequestDto request) throws ValidacaoTransacaoException {
        String numeroCartao = request.getNumeroCartao();
        log.info("Verificando existencia de cartao para o número {}", numeroCartao);
        try {
            this.cartaoService.getCartao(numeroCartao);
        } catch (CartaoNotFoundException e) {
            String errorMessage = this.mensagensService.getErrorMessage("error.cartao.not.found", numeroCartao);
            log.info(errorMessage);
            throw new ValidacaoTransacaoException(errorMessage, RegrasAutorizacaoTransacao.CARTAO_INEXISTENTE);
        }
        log.info("Cartao informado {} existe", numeroCartao);

    }
}
