package br.com.desafiovr.miniautorizador.service.processador;

import br.com.desafiovr.miniautorizador.enums.RegrasAutorizacaoTransacao;
import br.com.desafiovr.miniautorizador.exceptions.SenhaInvalidaException;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import br.com.desafiovr.miniautorizador.service.CartaoService;
import br.com.desafiovr.miniautorizador.service.MensagensService;
import br.com.desafiovr.miniautorizador.service.transacoes.processador.ValidaSenhaTransacao;
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
 class ValidaSenhaTransacaoTest {


    @InjectMocks
    private ValidaSenhaTransacao validaSenhaTransacao;

    @Mock
    private MensagensService mensagensService;

    @Mock
    private CartaoService cartaoService;


    @Test
     void Deve_ValidarSenhaTransacao() throws Exception {
        String numeroValido = "1234567890123456";
        String senhaInvalida = "123456";

        this.validaSenhaTransacao.validar(buildTransacaoValida(numeroValido, senhaInvalida));

        verify(cartaoService).validaSenha(numeroValido, senhaInvalida);
    }

    @Test
     void Deve_RetornarErro_Quando_SenhaInvalida() throws Exception {
        String numeroValido = "1234567890123456";
        String senhaInvalida = "123456";


        Mockito.doThrow(new SenhaInvalidaException("Senha inválida"))
                .when(cartaoService).validaSenha(numeroValido, senhaInvalida);

        ValidacaoTransacaoException validacaoTransacaoException = assertThrows(ValidacaoTransacaoException.class,
                () -> this.validaSenhaTransacao.validar(buildTransacaoValida(numeroValido, senhaInvalida)));

        assertNotNull(validacaoTransacaoException);
        assertEquals(RegrasAutorizacaoTransacao.SENHA_INVALIDA, validacaoTransacaoException.getRegrasAutorizacaoTransacao());

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
