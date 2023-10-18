package br.com.desafiovr.miniautorizador.service.processador;

import br.com.desafiovr.miniautorizador.enums.RegrasAutorizacaoTransacao;
import br.com.desafiovr.miniautorizador.exceptions.SaldoInsuficienteException;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import br.com.desafiovr.miniautorizador.service.CartaoService;
import br.com.desafiovr.miniautorizador.service.transacoes.processador.ValidaSaldoTransacao;
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
class ValidaSaldoTransacaoTest {


    @InjectMocks
    private ValidaSaldoTransacao validaSaldoTransacao;

    @Mock
    private CartaoService cartaoService;


    @Test
    void Deve_ValidarSaldoTransacao() throws Exception {
        String numeroValido = "1234567890123456";
        String senhaValida = "123456";

        this.validaSaldoTransacao.validar(buildTransacaoValida(numeroValido, senhaValida));

        verify(cartaoService).validaSaldoDisponivel(numeroValido);
    }

    @Test
    void Deve_RetornarErro_Quando_CartaoInvalida() throws Exception {
        String numeroValido = "1234567890123456";
        String senhaValida = "123456";

        Mockito.doThrow(new SaldoInsuficienteException("Saldo Insuficiente"))
                .when(cartaoService).validaSaldoDisponivel(numeroValido);

        ValidacaoTransacaoException validacaoTransacaoException = assertThrows(ValidacaoTransacaoException.class,
                () -> this.validaSaldoTransacao.validar(buildTransacaoValida(numeroValido, senhaValida)));

        assertNotNull(validacaoTransacaoException);
        assertEquals(RegrasAutorizacaoTransacao.SALDO_INSUFICIENTE, validacaoTransacaoException.getRegrasAutorizacaoTransacao());

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
