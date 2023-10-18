package br.com.desafiovr.miniautorizador.service.processador;

import br.com.desafiovr.miniautorizador.enums.RegrasAutorizacaoTransacao;
import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.exceptions.SenhaInvalidaException;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import br.com.desafiovr.miniautorizador.service.CartaoService;
import br.com.desafiovr.miniautorizador.service.MensagensService;
import br.com.desafiovr.miniautorizador.service.transacoes.processador.ValidaCartaoTransacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ValidaCartaoTransacaoTest {


    @InjectMocks
    private ValidaCartaoTransacao validaCartaoTransacao;

    @Mock
    private MensagensService mensagensService;

    @Mock
    private CartaoService cartaoService;


    @Test
    void Deve_ValidarSenhaTransacao() throws Exception {
        String numeroValido = "1234567890123456";
        String senhaValida = "123456";

        this.validaCartaoTransacao.validar(buildTransacaoValida(numeroValido, senhaValida));

        verify(cartaoService).getCartao(numeroValido);
    }

    @Test
    void Deve_RetornarErro_Quando_CartaoInvalida() throws Exception {
        String numeroValido = "1234567890123456";
        String senhaValida = "123456";

        Mockito.doThrow(new CartaoNotFoundException("Cartao não existe"))
                .when(cartaoService).getCartao(numeroValido);

        ValidacaoTransacaoException validacaoTransacaoException = assertThrows(ValidacaoTransacaoException.class,
                () -> this.validaCartaoTransacao.validar(buildTransacaoValida(numeroValido, senhaValida)));

        assertNotNull(validacaoTransacaoException);
        assertEquals(RegrasAutorizacaoTransacao.CARTAO_INEXISTENTE, validacaoTransacaoException.getRegrasAutorizacaoTransacao());

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
