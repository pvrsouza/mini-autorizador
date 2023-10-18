package br.com.desafiovr.miniautorizador.service.transacoes;

import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.locker.LockDistribuido;
import br.com.desafiovr.miniautorizador.locker.ExecucaoLockResultado;
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

    private final LockDistribuido locker;

    public TransacaoServiceImpl(CadeiaValidacoesTransacao cadeiaValidacoesTransacao, CartaoService cartaoService, LockDistribuido locker) {
        this.cadeiaValidacoesTransacao = cadeiaValidacoesTransacao;
        this.cartaoService = cartaoService;
        this.locker = locker;
    }

    @Override
    public void registra(TransacaoRequestDto transacao) throws ValidacaoTransacaoException {

        String numeroCartao = transacao.getNumeroCartao();

        log.info("Executando o registro da transacao - cartão {} ", numeroCartao);

        ExecucaoLockResultado<String> result = locker.lock(numeroCartao, 10, 10, () -> {

            //caso seja necessário validar se está aconecendo race condiciont basta incluir um Thread.sleep(1000).
            log.info("Transação INICIADA - cartão {}", numeroCartao);
            this.cadeiaValidacoesTransacao.execute(transacao);

            log.info("Atualizando saldo - cartão {}", numeroCartao);
            this.atualizaSaldo(transacao);

            return numeroCartao;
        });

        log.info("Transação REALIZADA - cartão: {} - execução falhou: {}", result.getResultadoBloqueioAdiquirido(), result.hasException());

        if (result.hasException()) {
            throw (ValidacaoTransacaoException) result.getException();
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
