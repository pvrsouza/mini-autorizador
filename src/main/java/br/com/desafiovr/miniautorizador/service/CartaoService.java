package br.com.desafiovr.miniautorizador.service;

import br.com.desafiovr.miniautorizador.exceptions.CartaoExistenteException;
import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.exceptions.SaldoInsuficienteException;
import br.com.desafiovr.miniautorizador.exceptions.SenhaInvalidaException;
import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public interface CartaoService {
    CartaoResponseDto create(CartaoRequestDto cartaoRequest) throws CartaoExistenteException;

    String getSaldo(String numeroCartao) throws CartaoNotFoundException;

    Cartao getCartao(String numeroCartao) throws CartaoNotFoundException;

    void validaSenha(String numeroCartao, String senha) throws SenhaInvalidaException;

    void validaSaldoDisponivel(String numeroCartao) throws SaldoInsuficienteException;

    Cartao atualizaSaldo(Cartao numeroCartao, BigDecimal resultado);
}
