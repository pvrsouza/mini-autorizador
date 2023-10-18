package br.com.desafiovr.miniautorizador.service.transacoes;

import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import br.com.desafiovr.miniautorizador.service.CartaoService;
import br.com.desafiovr.miniautorizador.service.transacoes.processador.CadeiaValidacoesTransacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class TransacaoServiceImpl implements TransacaoService {

    private final CadeiaValidacoesTransacao cadeiaValidacoesTransacao;

    private final CartaoService cartaoService;

    public TransacaoServiceImpl(CadeiaValidacoesTransacao cadeiaValidacoesTransacao, CartaoService cartaoService) {
        this.cadeiaValidacoesTransacao = cadeiaValidacoesTransacao;
        this.cartaoService = cartaoService;
    }

    @Override
    public void registra(TransacaoRequestDto transacao) throws ValidacaoTransacaoException, CartaoNotFoundException {
        //TODO: tentar fazer algum tipo de bloqueio da transacao com um lock centalizado na base que resolva race condition entre aplicações diferentes
        //TODO: O lock deve ser feito aqui, antes de iniciar a cadeia de validações pois a cadeia de validações pode demorar um pouco e o saldo pode mudar entre a validao e a atualização do saldo
        log.info("Transação INICIADA - cartão {}", transacao.getNumeroCartao());
        this.cadeiaValidacoesTransacao.execute(transacao);

        log.info("Atualizando saldo - cartão {}", transacao.getNumeroCartao());
        this.atualizaSaldo(transacao);

        log.info("Transação REALIZADA com sucesso - cartão {}", transacao.getNumeroCartao());
    }

    private void atualizaSaldo(TransacaoRequestDto transacaoRequestDto) throws CartaoNotFoundException {
        String numeroCartao = transacaoRequestDto.getNumeroCartao();
        Cartao cartao = this.cartaoService.getCartao(numeroCartao);
        BigDecimal saldoAnterior = cartao.getSaldo();

        BigDecimal resultado = cartao.getSaldo().subtract(transacaoRequestDto.getValor());

        this.cartaoService.atualizaSaldo(cartao, resultado);

        log.info("Saldo atualizado com sucesso no cartao {}. Saldo anterior [{}] Saldo atual [{}]",numeroCartao, saldoAnterior, cartao.getSaldo());
    }
}
