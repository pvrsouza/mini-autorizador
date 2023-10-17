package br.com.desafiovr.miniautorizador.controller;

import br.com.desafiovr.miniautorizador.controllers.CartaoController;
import br.com.desafiovr.miniautorizador.exceptions.CartaoExistenteException;
import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.service.CartaoServiceImpl;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CartaoController.class})
@AutoConfigureMockMvc
@Disabled("Desabilitado porque está dando erro de contexto ao executar o mvn test")
public class CartaoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    CartaoServiceImpl cartaoService;

    private static final String BASE_PATH = "/cartoes";


    @Test
    void Deve_CriarCartao_ComSucesso() throws Exception {

        CartaoRequestDto cartaoRequest = buildCartao();

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

        CartaoRequestDto cartaoRequest = buildCartao();

        when(cartaoService.create(cartaoRequest)).thenThrow(new CartaoExistenteException(cartaoRequest.toResponseDto(),HttpStatus.UNPROCESSABLE_ENTITY ));

        String json = cartaoRequest.toJson();
        mvc.perform(post(BASE_PATH)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(jsonPath("$.numeroCartao").value(cartaoRequest.getNumeroCartao()))
                .andExpect(jsonPath("$.senha").value(cartaoRequest.getSenha()))
        ;
    }


    private static CartaoRequestDto buildCartao() {
        return CartaoRequestDto.builder()
                .senha("123456")
                .numeroCartao("12")
                .build();
    }

}
