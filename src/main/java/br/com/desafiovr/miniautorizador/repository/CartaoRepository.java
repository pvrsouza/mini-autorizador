package br.com.desafiovr.miniautorizador.repository;

import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CartaoRepository extends JpaRepository<Cartao, Long>{
    Optional<Cartao> findByNumeroCartao(String numeroCartao);

    Optional<Cartao> findByNumeroCartaoAndSenha(String numeroCartao, String senha);
    Optional<Cartao> findByNumeroCartaoAndSaldoGreaterThan(String numeroCartao, BigDecimal valorMinimo);
}
