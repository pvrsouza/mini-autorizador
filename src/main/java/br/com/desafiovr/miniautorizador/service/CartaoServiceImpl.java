package br.com.desafiovr.miniautorizador.service;

import br.com.desafiovr.miniautorizador.exceptions.CartaoExistenteException;
import br.com.desafiovr.miniautorizador.exceptions.CartaoNotFoundException;
import br.com.desafiovr.miniautorizador.exceptions.SaldoInsuficienteException;
import br.com.desafiovr.miniautorizador.exceptions.SenhaInvalidaException;
import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import br.com.desafiovr.miniautorizador.repository.CartaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class CartaoServiceImpl implements CartaoService {

    private final CartaoRepository repository;

    private final MensagensService mensagensService;

    public CartaoServiceImpl(CartaoRepository repository, MensagensService mensagensService) {
        this.repository = repository;
        this.mensagensService = mensagensService;
    }

    @Override
    public CartaoResponseDto create(CartaoRequestDto cartaoRequest) throws CartaoExistenteException {

        Objects.requireNonNull(cartaoRequest, this.mensagensService.getNullPointerErrorMessage());

        try {

            Cartao cartao = cartaoRequest.toEntity();

            Cartao savedCartao = this.repository.save(cartao);
            log.info("Cartão salvo com sucesso. Cartão [{}]", cartao);

            return savedCartao.toResponseDto();

        } catch (DataIntegrityViolationException e) {

            String errorMessage = this.mensagensService.getErrorMessage(
                    "error.cartao.existente",
                    cartaoRequest.getNumeroCartao());
            log.error(errorMessage, e);

            throw new CartaoExistenteException(cartaoRequest.toResponseDto(), HttpStatus.UNPROCESSABLE_ENTITY);

        } catch (Exception e) {
            log.error("Erro ao salvar cartão", e);
            throw e;
        }

    }

    @Override
    public String getSaldo(String numeroCartao) throws CartaoNotFoundException {
        Cartao cartao = getCartao(numeroCartao);
        return cartao.getSaldo().setScale(2, BigDecimal.ROUND_HALF_DOWN).toString();
    }

    public Cartao getCartao(String numeroCartao) throws CartaoNotFoundException {

        Assert.notNull(numeroCartao, this.mensagensService.getErrorMessage("error.cartao.numero.nulo"));
        Assert.isTrue(StringUtils.hasText(numeroCartao), this.mensagensService.getErrorMessage("error.cartao.numero.vazio"));

        return this.repository.findByNumeroCartao(numeroCartao)
                .orElseThrow(() -> new CartaoNotFoundException(this.mensagensService.getErrorMessage("error.cartao.not.found", numeroCartao)));
    }

    @Override
    public void validaSenha(String numeroCartao, String senha) throws SenhaInvalidaException {
        this.repository.findByNumeroCartaoAndSenha(numeroCartao, senha).ifPresentOrElse(
                c -> log.info("Senha valida para o cartão {}", numeroCartao),
                () -> {
                    String errorMessage = this.mensagensService.getErrorMessage("error.cartao.senha.invalida");
                    log.error(errorMessage);
                    throw new SenhaInvalidaException(errorMessage);
                }
        );
    }

    @Override
    public void validaSaldoDisponivel(String numeroCartao, BigDecimal valorOperacao) throws SaldoInsuficienteException {
        Optional<Cartao> byNumeroCartaoAndSaldoGreaterThan = this.repository.findByNumeroCartaoAndSaldoMaiorQueZeroAndSaldoMaiorQueOperacao(numeroCartao, valorOperacao);

        byNumeroCartaoAndSaldoGreaterThan.ifPresentOrElse(
                c -> log.info("Saldo disponível para a operação no cartão de número {}", numeroCartao),
                () -> {
                    String errorMessage = this.mensagensService.getErrorMessage("error.cartao.saldo.insuficiente");
                    log.error(errorMessage);
                    throw new SaldoInsuficienteException(errorMessage);
                }
        );
    }

    @Override
    public Cartao atualizaSaldo(Cartao cartao, BigDecimal resultado) {
        cartao.setSaldo(resultado);
        return this.repository.save(cartao);
    }
}
