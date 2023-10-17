package br.com.desafiovr.miniautorizador.service;

import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import br.com.desafiovr.miniautorizador.repository.CartaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartaoServiceUnitTest {

    @InjectMocks
    private CartaoServiceImpl cartaoService;

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private MensagensService mensagensService;

    @Captor
    ArgumentCaptor<Cartao> cartaoArgumentCaptor;


    @Test
    public void NaoDeve_IncluirCartao_Quando_PassarObjetoNuloComoParametro() {

        String nullPointerErrorMessage = "O objeto informado não pode ser nulo";

        when(this.mensagensService.getNullPointerErrorMessage()).thenReturn(nullPointerErrorMessage);

        NullPointerException nullPointerException =
                Assertions.assertThrows(
                        NullPointerException.class,
                        () -> this.cartaoService.create(null)
                );

        assertEquals(nullPointerErrorMessage, nullPointerException.getMessage());
    }

    @Test
    public void Deve_IncluirCartao() throws Exception{
        CartaoRequestDto cartaoRequestDto = CartaoRequestDto.builder()
                .numeroCartao("1234567890123456")
                .senha("123456")
                .build();

        Cartao cartaoEntity =  cartaoRequestDto.toEntity();

        Cartao cartaoSaved = Cartao.builder()
                .id(1L)
                .numeroCartao(cartaoEntity.getNumeroCartao())
                .senha(cartaoEntity.getSenha())
                .build();

        when(this.cartaoRepository.save(cartaoEntity)).thenReturn(cartaoSaved);

        CartaoResponseDto response = this.cartaoService.create(cartaoRequestDto);

        verify(this.cartaoRepository).save(cartaoArgumentCaptor.capture());
        assertEquals(cartaoEntity, cartaoArgumentCaptor.getValue());
        assertEquals(cartaoEntity.toResponseDto(), response);

    }

    @Test
    public void Deve_Retornar_SaldoFormatado() throws Exception{
        String numeroCartao = "1234567890123456";
        String valorFormatado = "500.00";

        Cartao cartao = buildCartaoValido(numeroCartao);
        when(this.cartaoRepository.findByNumeroCartao(numeroCartao)).thenReturn(Optional.of(cartao));

        String saldoFormatado = this.cartaoService.getSaldo(numeroCartao);
        assertEquals(valorFormatado, saldoFormatado);
    }

    @Test
    public void Deve_RetornarErro_Quando_TentarObterSaldoComParametroVazio() throws Exception{
        String numeroCartao = "";

        Cartao cartao = buildCartaoValido(numeroCartao);

        Exception exception = Assertions.assertThrows(Exception.class, () -> this.cartaoService.getSaldo(numeroCartao));

        assertNotNull(exception);
    }

    private static Cartao buildCartaoValido(String numeroCartao) {
        return Cartao.builder()
                .id(1L)
                .numeroCartao(numeroCartao)
                .senha("123456")
                .saldo(new BigDecimal(500))
                .build();
    }


}
