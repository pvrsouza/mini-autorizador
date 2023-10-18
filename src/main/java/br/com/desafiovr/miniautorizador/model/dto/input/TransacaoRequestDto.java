package br.com.desafiovr.miniautorizador.model.dto.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransacaoRequestDto {

    @NotEmpty(message = "O campo número do cartão é obrigatório.")
    @Schema(description = "Campo que representa o número do cartão.", example = "123456789", required = true)
    @Size(min = 16, max = 20, message = "É necessário informar no mínimo 16 e no máximo 20 caracteres pra o número do cartão")
    private String numeroCartao;

    @NotEmpty(message = "O campo senha é obrigatório.")
    @Schema(description = "Campo que representa a senha do cartão.", example = "123456789", required = true)
    @Size(min = 4, message = "É necessário informar no mínimo 4 caracteres pra o número do cartão")
    private String senhaCartao;

    @NotNull(message = "O campo valor é obrigatório.")
    @Schema(description = "Campo que representa a valor da transação.", example = "1321645.21", required = true)
    private BigDecimal valor;
}
