package br.com.desafiovr.miniautorizador.service;


import br.com.desafiovr.miniautorizador.enums.RegrasAutorizacaoTransacao;
import br.com.desafiovr.miniautorizador.exceptions.SenhaInvalidaException;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import br.com.desafiovr.miniautorizador.service.transacoes.TransacaoServiceImpl;
import br.com.desafiovr.miniautorizador.service.transacoes.processador.CadeiaValidacoesTransacao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransacaoServiceUnitTest {

    @InjectMocks
    private TransacaoServiceImpl transacaoService;

    @Mock
    private CadeiaValidacoesTransacao cadeiaValidacoesTransacao;

    @Mock
    private CartaoService cartaoService;

    @Captor
    ArgumentCaptor<Cartao> cartaoArgumentCaptor;

    @Test
    public void Deve_RegistrarTransacao_Quando_TransacaoValida() throws Exception{
        String numeroValido = "1234567890123456";
        String senhaValida = "123456";

        TransacaoRequestDto transacao = buildTransacaoValida(numeroValido, senhaValida);

        Cartao cartao = buildCartaoValido(senhaValida, numeroValido);

        Mockito.when(this.cartaoService.getCartao(numeroValido)).thenReturn(cartao);

        doNothing().when(this.cadeiaValidacoesTransacao).execute(transacao);

        this.transacaoService.registra(transacao);

        verify(this.cartaoService).atualizaSaldo(any(Cartao.class), any(BigDecimal.class));


    }


    @Test
    public void Deve_RegistrarTransacao_Quando_TransacaoInvalida_SenhaIncorreta() throws Exception{
        String numeroValido = "1234567890123456";
        String senhaInvalida = "XXXXXX";

        TransacaoRequestDto transacao = buildTransacaoValida(numeroValido, senhaInvalida);

        doThrow(new ValidacaoTransacaoException("Senha invalida", RegrasAutorizacaoTransacao.SENHA_INVALIDA))
                .when(this.cadeiaValidacoesTransacao).execute(transacao);

        ValidacaoTransacaoException validacaoTransacaoException
                = Assertions.assertThrows(ValidacaoTransacaoException.class, () -> this.transacaoService.registra(transacao));

        assertNotNull(validacaoTransacaoException);
        verify(this.cartaoService, never()).atualizaSaldo(any(Cartao.class), any(BigDecimal.class));
    }

    @Test
    public void Deve_RegistrarTransacao_Quando_TransacaoInvalida_SaldoInsuficiente() throws Exception{
        String numeroValido = "1234567890123456";
        String senhaInvalida = "123456";

        TransacaoRequestDto transacao = buildTransacaoValida(numeroValido, senhaInvalida);

        doThrow(new ValidacaoTransacaoException("Saldo insuficiente", RegrasAutorizacaoTransacao.SALDO_INSUFICIENTE))
                .when(this.cadeiaValidacoesTransacao).execute(transacao);

        ValidacaoTransacaoException validacaoTransacaoException
                = Assertions.assertThrows(ValidacaoTransacaoException.class, () -> this.transacaoService.registra(transacao));

        assertNotNull(validacaoTransacaoException);
        verify(this.cartaoService, never()).atualizaSaldo(any(Cartao.class), any(BigDecimal.class));
    }

    @Test
    public void Deve_RegistrarTransacao_Quando_TransacaoInvalida_SaldoCartaoInexistente() throws Exception{
        String numeroValido = "1234567890123456";
        String senhaInvalida = "XXXXXX";

        TransacaoRequestDto transacao = buildTransacaoValida(numeroValido, senhaInvalida);

        doThrow(new ValidacaoTransacaoException("Cartão não existe", RegrasAutorizacaoTransacao.CARTAO_INEXISTENTE))
                .when(this.cadeiaValidacoesTransacao).execute(transacao);

        ValidacaoTransacaoException validacaoTransacaoException
                = Assertions.assertThrows(ValidacaoTransacaoException.class, () -> this.transacaoService.registra(transacao));

        assertNotNull(validacaoTransacaoException);
        verify(this.cartaoService, never()).atualizaSaldo(any(Cartao.class), any(BigDecimal.class));
    }

    private static Cartao buildCartaoValido(String senhaValida, String numeroValido) {
        return Cartao.builder()
                .senha(senhaValida)
                .numeroCartao(numeroValido)
                .saldo(BigDecimal.TEN)
                .build();
    }

    private TransacaoRequestDto buildTransacaoValida(String numeroValido, String senhaValida) {

        BigDecimal valorValido = BigDecimal.TEN;

        return TransacaoRequestDto.builder()
                .numeroCartao(numeroValido)
                .senhaCartao(senhaValida)
                .valor(valorValido)
                .build();
    }

}
