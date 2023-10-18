package br.com.desafiovr.miniautorizador.service.transacoes;

import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import br.com.desafiovr.miniautorizador.service.CartaoService;
import br.com.desafiovr.miniautorizador.service.transacoes.processador.CadeiaValidacoesTransacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;

@Service
@Slf4j
public class TransacaoServiceImpl implements TransacaoService {

    private final CadeiaValidacoesTransacao cadeiaValidacoesTransacao;

    private final CartaoService cartaoService;

    private final LockRegistry lockRegistry;

    public TransacaoServiceImpl(CadeiaValidacoesTransacao cadeiaValidacoesTransacao, CartaoService cartaoService, LockRegistry lockRegistry) {
        this.cadeiaValidacoesTransacao = cadeiaValidacoesTransacao;
        this.cartaoService = cartaoService;
        this.lockRegistry = lockRegistry;
    }

    @Override
    public void registra(TransacaoRequestDto transacao) throws ValidacaoTransacaoException, CartaoNotFoundException {
        var lock = lockRegistry.obtain(transacao.getNumeroCartao());
        boolean lockAdiquirido = lock.tryLock();
        if (lockAdiquirido) {
            try {
                log.info("Transação INICIADA - cartão {}", transacao.getNumeroCartao());
                this.cadeiaValidacoesTransacao.execute(transacao);

                log.info("Atualizando saldo - cartão {}", transacao.getNumeroCartao());
                this.atualizaSaldo(transacao);

                log.info("Transação REALIZADA com sucesso - cartão {}", transacao.getNumeroCartao());
            } finally {
                lock.unlock();
            }
        }
    }

    private void atualizaSaldo(TransacaoRequestDto transacaoRequestDto) throws CartaoNotFoundException {
        String numeroCartao = transacaoRequestDto.getNumeroCartao();
        Cartao cartao = this.cartaoService.getCartao(numeroCartao);
        BigDecimal saldoAnterior = cartao.getSaldo();

        BigDecimal resultado = cartao.getSaldo().subtract(transacaoRequestDto.getValor());

        this.cartaoService.atualizaSaldo(cartao, resultado);

        log.info("Saldo atualizado com sucesso no cartao {}. Saldo anterior [{}] Saldo atual [{}]", numeroCartao, saldoAnterior, cartao.getSaldo());
    }
}
