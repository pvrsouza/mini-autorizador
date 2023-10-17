package br.com.desafiovr.miniautorizador.model.entity;

import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
@Entity
@Table(name = "tb_cartao")
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cartao")
    private Integer id;

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
