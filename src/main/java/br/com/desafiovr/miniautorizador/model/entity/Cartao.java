package br.com.desafiovr.miniautorizador.model.entity;

import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

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

    @Column(name="saldo", nullable = false)
    private BigDecimal saldo;

    @PrePersist
    public void prePersist() {
        saldo = new BigDecimal(500);
    }

    public CartaoResponseDto toResponseDto(){
        return CartaoResponseDto.builder()
                .numeroCartao(this.numeroCartao)
                .senha(this.senha)
                .build();
    }

}
