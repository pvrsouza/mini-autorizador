package br.com.desafiovr.miniautorizador.controllers;

import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.ErrorResponse;
import br.com.desafiovr.miniautorizador.service.transacoes.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Operation(summary = "Registra uma transação")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Transação realizada com sucesso",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class))}),
            @ApiResponse(
                    responseCode = "422",
                    description = "Alguma regra de autorização não foi atendida impedindo a realização da transação.",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class))}),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ocorreu um erro inesperado.",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))})

    })
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> create(@Valid @RequestBody TransacaoRequestDto transacaoRequestDto) throws CartaoNotFoundException {

        try {
            this.transacaoService.registra(transacaoRequestDto);
        } catch (CartaoNotFoundException e) {
            throw e;
        } catch (ValidacaoTransacaoException e) {
            return new ResponseEntity<>(e.getRegrasAutorizacaoTransacao().name(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>("OK", HttpStatus.CREATED);
    }
}
