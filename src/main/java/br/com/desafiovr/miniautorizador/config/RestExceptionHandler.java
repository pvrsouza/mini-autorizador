package br.com.desafiovr.miniautorizador.config;

import br.com.desafiovr.miniautorizador.exceptions.CartaoExistenteException;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.dto.output.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    private ResponseEntity<ErrorResponse> handleExceptionInternal(Exception ex, ErrorResponse errorResponse, HttpStatus httpStatus) {
        logException(ex);
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<ErrorResponse> handleBusinessException(Exception ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .codigo("99")
                .descricao("Ocorreu um erro inesperado. Entre em contato com o suporte técnico")
                .build();
        return handleExceptionInternal(ex, errorResponse , HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {CartaoExistenteException.class})
    protected ResponseEntity<CartaoResponseDto> handleCartaoExistenteException(CartaoExistenteException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getCartaoResponseDto(), ex.getHttpStatus());
    }

    private static void logException(Exception ex) {
        log.error("Exception: {}", ex.getMessage());
        ex.printStackTrace();
    }
}
