package br.com.desafiovr.miniautorizador.service;

import br.com.desafiovr.miniautorizador.exceptions.SaldoInsuficienteException;
import br.com.desafiovr.miniautorizador.exceptions.SenhaInvalidaException;
import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import br.com.desafiovr.miniautorizador.repository.CartaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartaoServiceUnitTest {

    @InjectMocks
    private CartaoServiceImpl cartaoService;

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private MensagensService mensagensService;

    @Captor
    ArgumentCaptor<Cartao> cartaoArgumentCaptor;


    @Test
    void NaoDeve_IncluirCartao_Quando_PassarObjetoNuloComoParametro() {

        String nullPointerErrorMessage = "O objeto informado não pode ser nulo";

        when(this.mensagensService.getNullPointerErrorMessage()).thenReturn(nullPointerErrorMessage);

        NullPointerException nullPointerException =
                assertThrows(
                        NullPointerException.class,
                        () -> this.cartaoService.create(null)
                );

        assertEquals(nullPointerErrorMessage, nullPointerException.getMessage());
    }

    @Test
    void Deve_IncluirCartao() throws Exception {
        CartaoRequestDto cartaoRequestDto = CartaoRequestDto.builder()
                .numeroCartao("1234567890123456")
                .senha("123456")
                .build();

        Cartao cartaoEntity = cartaoRequestDto.toEntity();

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
    void Deve_Retornar_SaldoFormatado() throws Exception {
        String numeroCartao = "1234567890123456";
        String valorFormatado = "500.00";

        Cartao cartao = buildCartaoValido(numeroCartao);
        when(this.cartaoRepository.findByNumeroCartao(numeroCartao)).thenReturn(Optional.of(cartao));

        String saldoFormatado = this.cartaoService.getSaldo(numeroCartao);
        assertEquals(valorFormatado, saldoFormatado);
    }

    @Test
    void Deve_RetornarErro_Quando_TentarObterSaldoComParametroVazio() throws Exception {
        String numeroCartao = "";

        Cartao cartao = buildCartaoValido(numeroCartao);

        Exception exception = assertThrows(Exception.class, () -> this.cartaoService.getSaldo(numeroCartao));

        assertNotNull(exception);
    }

    @Test
    void Deve_RetornarErro_Quando_TentarObterSaldoComParametroNulo() throws Exception {
        String numeroCartao = null;

        Exception exception = assertThrows(Exception.class,
                () -> this.cartaoService.getSaldo(numeroCartao));

        assertNotNull(exception);
    }

    @Test
    void Deve_ValidarSenha_Valida() {
        String numeroCartao = "1234567890123456";
        String senha = "123456";

        Cartao cartao = buildCartaoValido(numeroCartao);
        when(this.cartaoRepository.findByNumeroCartaoAndSenha(numeroCartao, senha))
                .thenReturn(Optional.of(cartao));

        this.cartaoService.validaSenha(numeroCartao, senha);
    }

    @Test
    void Deve_RetornarErro_Quando_ValidarSenhaInvalida() {
        String numeroCartao = "1234567890123456";
        String senha = "123456";

        when(this.cartaoRepository.findByNumeroCartaoAndSenha(numeroCartao, senha))
                .thenReturn(Optional.empty());

        SenhaInvalidaException exception = assertThrows(SenhaInvalidaException.class,
                () -> this.cartaoService.validaSenha(numeroCartao, senha));

        assertNotNull(exception);
    }

    @Test
    void Deve_Validar_SaldoDisponivel() {

        String numeroCartao = "1234567890123456";

        Cartao cartaoValido = buildCartaoValido(numeroCartao);

        when(cartaoRepository.findByNumeroCartaoAndSaldoGreaterThan(numeroCartao, BigDecimal.ZERO))
                .thenReturn(Optional.of(cartaoValido));

        this.cartaoService.validaSaldoDisponivel(numeroCartao);

    }


    @Test
    void Deve_RetornarErro_Quando_CartaoNaoTiverSaldoDisponivel() {

        String numeroCartao = "1234567890123456";

        when(cartaoRepository.findByNumeroCartaoAndSaldoGreaterThan(numeroCartao, BigDecimal.ZERO))
                .thenReturn(Optional.empty());

        SaldoInsuficienteException exception = assertThrows(SaldoInsuficienteException.class,
                () -> this.cartaoService.validaSaldoDisponivel(numeroCartao));

        assertNotNull(exception);

    }


    @Test
    void Deve_AtualizarSaldoCartao() {
        String numeroCartao = "1234567890123456";
        BigDecimal saldoNovo = new BigDecimal(1000);
        Cartao cartao = buildCartaoValido(numeroCartao);

        this.cartaoService.atualizaSaldo(cartao, saldoNovo);

        verify(this.cartaoRepository).save(cartaoArgumentCaptor.capture());

        Cartao cartaoArgumentCaptorValue = cartaoArgumentCaptor.getValue();
        assertEquals(cartao, cartaoArgumentCaptorValue);
        assertEquals(saldoNovo, cartaoArgumentCaptorValue.getSaldo());
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
