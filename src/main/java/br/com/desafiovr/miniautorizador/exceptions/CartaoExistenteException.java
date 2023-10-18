package br.com.desafiovr.miniautorizador.exceptions;

import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CartaoExistenteException extends Exception {

    private final HttpStatus httpStatus;
    private final CartaoResponseDto cartaoResponseDto;

    public CartaoExistenteException(CartaoResponseDto cartaoResponseDto, HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.cartaoResponseDto = cartaoResponseDto;
    }




}
