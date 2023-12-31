package br.com.desafiovr.miniautorizador.controller;

import br.com.desafiovr.miniautorizador.controllers.CartaoController;
import br.com.desafiovr.miniautorizador.exceptions.CartaoExistenteException;
import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.repository.CartaoRepository;
import br.com.desafiovr.miniautorizador.service.CartaoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CartaoController.class})
@AutoConfigureMockMvc
class CartaoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    CartaoServiceImpl cartaoService;

    private static final String BASE_PATH = "/cartoes";
    private static final String SALDO_PATH = "/{numeroCartao}";


    @Test
    void Deve_CriarCartao_ComSucesso() throws Exception {

        CartaoRequestDto cartaoRequest = buildCartaoRequestValido();

        when(cartaoService.create(cartaoRequest)).thenReturn(cartaoRequest.toResponseDto());

        String json = cartaoRequest.toJson();
        mvc.perform(post(BASE_PATH)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.numeroCartao").value(cartaoRequest.getNumeroCartao()))
                .andExpect(jsonPath("$.senha").value(cartaoRequest.getSenha()))
        ;
    }


    @Test
    void NaoDeve_CriarCartao_Quando_NumeroCartaoJaEstiverCadastrado() throws Exception {

        CartaoRequestDto requestValida = buildCartaoRequestValido();

        when(cartaoService.create(requestValida)).thenThrow(new CartaoExistenteException(requestValida.toResponseDto(),HttpStatus.UNPROCESSABLE_ENTITY ));

        String json = requestValida.toJson();
        mvc.perform(post(BASE_PATH)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(jsonPath("$.numeroCartao").value(requestValida.getNumeroCartao()))
                .andExpect(jsonPath("$.senha").value(requestValida.getSenha()))
        ;
    }

    @Test
    void NaoDeve_CriarCartao_Quando_NaoAtenderAosCriteriosDeValidacaoDoInputRequest() throws Exception {

        CartaoRequestDto requestInvalida = buildCartaoRequestInvalido();


        String json = requestInvalida.toJson();
        mvc.perform(post(BASE_PATH)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.descricao").isNotEmpty())
        ;
    }

    @Test
    void Deve_Obter_SaldoCartao() throws Exception {

        String numeroCartao = "1234567890123456";
        String saldoCartao = "500.00";

        when(this.cartaoService.getSaldo(numeroCartao)).thenReturn(saldoCartao);

        MvcResult result = mvc.perform(get(BASE_PATH + SALDO_PATH, numeroCartao)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        assertEquals(saldoCartao, content);
    }


    private static CartaoRequestDto buildCartaoRequestValido() {
        return CartaoRequestDto.builder()
                .senha("123456")
                .numeroCartao("1234567890123456")
                .build();
    }


    /**
     * Retorna um cartão inválido para testes com dados menores do que os exigidos
     * @return
     */
    private static CartaoRequestDto buildCartaoRequestInvalido() {
        return CartaoRequestDto.builder()
                .senha("1")
                .numeroCartao("1")
                .build();
    }

}
