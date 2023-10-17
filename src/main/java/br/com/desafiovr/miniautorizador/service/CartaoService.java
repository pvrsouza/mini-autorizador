package br.com.desafiovr.miniautorizador.service;

import br.com.desafiovr.miniautorizador.exceptions.CartaoExistenteException;
import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import org.springframework.web.bind.annotation.RestController;

@RestController
public interface CartaoService {
    CartaoResponseDto create(CartaoRequestDto cartaoRequest) throws CartaoExistenteException;

    String getSaldo(String numeroCartao) throws CartaoNotFoundException;

}
