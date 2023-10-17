package br.com.desafiovr.miniautorizador.model.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartaoResponseDto {

    private String senha;
    private String numeroCartao;
}
