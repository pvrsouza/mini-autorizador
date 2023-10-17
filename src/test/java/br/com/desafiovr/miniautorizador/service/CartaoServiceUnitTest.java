package br.com.desafiovr.miniautorizador.service;

import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import br.com.desafiovr.miniautorizador.repository.CartaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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


}
