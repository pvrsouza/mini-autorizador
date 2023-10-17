package br.com.desafiovr.miniautorizador.model.dto.input;

import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartaoRequestDto {

    @NotEmpty(message = "O campo número do cartão é obrigatório.")
    @Schema(description = "Campo que representa o número do cartão.", example = "123456789", required = true)
    @Size(min = 16, max = 20, message = "É necessário informar no mínimo 16 e no máximo 20 caracteres pra o número do cartão")
    private String numeroCartao;

    @NotEmpty(message = "O campo senha é obrigatório.")
    @Schema(description = "Campo que representa a senha do cartão.", example = "123456789", required = true)
    @Size(min = 6, max = 20, message = "É necessário informar no mínimo 6 e no máximo 20 caracteres pra o número do cartão")
    private String senha;

    public Cartao toEntity() {
        return Cartao.builder()
                .numeroCartao(this.numeroCartao)
                .senha(this.senha)
                .build();
    }

    public CartaoResponseDto toResponseDto() {
        return CartaoResponseDto.builder()
                .numeroCartao(this.numeroCartao)
                .senha(this.senha)
                .build();
    }

    public String toJson(){
        return new ObjectMapper().valueToTree(this).toString();
    }
}