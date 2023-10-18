package br.com.desafiovr.miniautorizador.service;

import br.com.desafiovr.miniautorizador.exceptions.CartaoExistenteException;
import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import br.com.desafiovr.miniautorizador.repository.CartaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ComponentScan("br.com.desafiovr.miniautorizador.service")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartaoServiceImplIntegrationTest {
    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CartaoServiceImpl cartaoService;

    @MockBean
    private MensagensService mensagensService;


    @BeforeEach
    public void beforeEach(){
        this.cartaoRepository.deleteAll();
    }


    @Test
    void Deve_ThrowsException_Quando_TentarSalvarCartaoComDadosInvalidos() {

        CartaoRequestDto invalidCartaoRequest = new CartaoRequestDto();

        Exception exception = Assertions.assertThrows(Exception.class, () -> cartaoService.create(invalidCartaoRequest));

        assertNotNull(exception);
    }

    @Test
    void Deve_SalvarCartao_Quando_PassarDadosValidos() throws Exception{

        CartaoRequestDto cartaoRequestDto = CartaoRequestDto.builder()
                .numeroCartao("1234567890123456")
                .senha("123456")
                .build();

        CartaoResponseDto cartaoResponseDto = cartaoService.create(cartaoRequestDto);

        assertEquals(1, cartaoRepository.count());

    }

    @Test
    void Deve_InserirSaldoDefault_Quando_CriarCartao() throws Exception{

        final BigDecimal saldoInicialParaCartoesNovos = new BigDecimal(500);

        CartaoRequestDto cartaoRequestDto = CartaoRequestDto.builder()
                .numeroCartao("1234567890123456")
                .senha("123456")
                .build();

        cartaoService.create(cartaoRequestDto);

        Optional<Cartao> cartaoPersistido = this.cartaoRepository.findByNumeroCartao(cartaoRequestDto.getNumeroCartao());

        assertNotNull(cartaoPersistido);
        assertTrue(cartaoPersistido.isPresent());
        assertEquals(saldoInicialParaCartoesNovos, cartaoPersistido.get().getSaldo());

    }

    @Test
    void Deve_ThrowsException_Quando_TentarSalvarCartaoExistente() throws Exception {
        String numeroCartao = "1234567890123456";

        // salva o cartão previamente
        CartaoRequestDto cartaoRequestDto = CartaoRequestDto.builder()
                .numeroCartao(numeroCartao)
                .senha("123456")
                .build();
        cartaoService.create(cartaoRequestDto);

        CartaoExistenteException exception = Assertions.assertThrows(CartaoExistenteException.class, () -> cartaoService.create(cartaoRequestDto));

        assertNotNull(exception);

    }


    @Test
    void Deve_Retornar_SaldoCartaoFormatado() throws Exception {
        String numeroCartao = "1234567890123456";

        // salva o cartão previamente
        CartaoRequestDto cartaoRequestDto = CartaoRequestDto.builder()
                .numeroCartao(numeroCartao)
                .senha("123456")
                .build();
        cartaoService.create(cartaoRequestDto);

        String saldoFormatado = cartaoService.getSaldo(numeroCartao);

        assertEquals("500.00", saldoFormatado);

    }
}
