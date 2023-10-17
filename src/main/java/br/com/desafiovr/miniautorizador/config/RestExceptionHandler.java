package br.com.desafiovr.miniautorizador.config;

import br.com.desafiovr.miniautorizador.exceptions.CartaoExistenteException;
import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.dto.output.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Optional;
import java.util.stream.Collectors;

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
                .descricao("Ocorreu um erro inesperado. Entre em contato com o suporte técnico")
                .build();
        return handleExceptionInternal(ex, errorResponse , HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {CartaoExistenteException.class})
    protected ResponseEntity<CartaoResponseDto> handleCartaoExistenteException(CartaoExistenteException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getCartaoResponseDto(), ex.getHttpStatus());
    }

    @ExceptionHandler(value = {CartaoNotFoundException.class})
    protected ResponseEntity<ErrorResponse> handleCartaoNotFoundException(CartaoNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .descricao(ex.getMessage())
                .build();
        return handleExceptionInternal(ex, errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex, WebRequest request){
        BindingResult bindingResult = ex.getBindingResult();
        String errorMsg = bindingResult.getFieldErrors().stream()
                .map(f -> f.getField().concat(": ").concat(Optional.ofNullable(f.getDefaultMessage()).orElse("")))
                .collect(Collectors.joining(" | "));
        ErrorResponse errorResponse = ErrorResponse.builder()
                .descricao(errorMsg)
                .build();

        return handleExceptionInternal(ex, errorResponse, HttpStatus.BAD_REQUEST);
    }


    private static void logException(Exception ex) {
        log.error("Exception: {}", ex.getMessage());
        ex.printStackTrace();
    }
}
