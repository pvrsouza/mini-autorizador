package br.com.desafiovr.miniautorizador.controllers;

import br.com.desafiovr.miniautorizador.exceptions.CartaoExistenteException;
import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.dto.output.ErrorResponse;
import br.com.desafiovr.miniautorizador.service.CartaoService;
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
@RequestMapping("/cartoes")
public class CartaoController {
    private final CartaoService cartaoService;

    public CartaoController(CartaoService cartaoService) {
        this.cartaoService = cartaoService;
    }

    @Operation(summary = "Cria o registro do contato")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cartao criado com sucesso",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CartaoResponseDto.class))}),
            @ApiResponse(
                    responseCode = "422",
                    description = "Já existe um cartão cadastrado para o número informado",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CartaoResponseDto.class))}),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ocorreu um erro inesperado.",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))})

    })
    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<CartaoResponseDto> create(@Valid @RequestBody CartaoRequestDto cartaoRequest)
            throws CartaoExistenteException {
        CartaoResponseDto cartao = this.cartaoService.create(cartaoRequest);
        return new ResponseEntity<>(cartao, HttpStatus.CREATED);
    }
}
