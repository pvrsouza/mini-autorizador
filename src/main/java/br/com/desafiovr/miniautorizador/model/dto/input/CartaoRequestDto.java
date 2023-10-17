package br.com.desafiovr.miniautorizador.model.dto.input;

import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartaoRequestDto {

    private String numeroCartao;
    private String senha;

    public Cartao toEntity(){
        return Cartao.builder()
                .numeroCartao(this.numeroCartao)
                .senha(this.senha)
                .build();
    }
}