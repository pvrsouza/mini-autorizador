package br.com.desafiovr.miniautorizador.model.entity;

import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "tb_cartao")
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cartao")
    private Long id;

    @Column(name="numero_cartao", nullable = false, unique = true)
    private String numeroCartao;

    @Column(name="senha", nullable = false)
    private String senha;

    public CartaoResponseDto toResponseDto(){
        return CartaoResponseDto.builder()
                .numeroCartao(this.numeroCartao)
                .senha(this.senha)
                .build();
    }

}
