package br.com.desafiovr.miniautorizador.service.transacoes.processador;

import br.com.desafiovr.miniautorizador.enums.RegrasAutorizacaoTransacao;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.exceptions.SaldoInsuficienteException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import br.com.desafiovr.miniautorizador.service.CartaoService;
import br.com.desafiovr.miniautorizador.service.MensagensService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;import java.math.BigDecimal;

@Service
@Slf4j
public class ValidaSaldoTransacao implements ValidacaoTransacao {

    private final CartaoService cartaoService;

    public ValidaSaldoTransacao(CartaoService cartaoService) {
        this.cartaoService = cartaoService;
    }

    @Override
    public void validar(TransacaoRequestDto request) throws ValidacaoTransacaoException {
        String numeroCartao = request.getNumeroCartao();
        log.info("Validando saldo para o cartão informado {}", numeroCartao);
        BigDecimal valoroperacao = request.getValor();
        try{
            this.cartaoService.validaSaldoDisponivel(numeroCartao, valoroperacao);
        }catch (SaldoInsuficienteException e){
            throw new ValidacaoTransacaoException(e.getMessage(), RegrasAutorizacaoTransacao.SALDO_INSUFICIENTE);
        }
        log.info("Saldo validado para o cartão informado {}", numeroCartao);
    }
}
