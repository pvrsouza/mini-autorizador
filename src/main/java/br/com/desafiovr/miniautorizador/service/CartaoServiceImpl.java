package br.com.desafiovr.miniautorizador.service;

import br.com.desafiovr.miniautorizador.exceptions.CartaoExistenteException;
import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import br.com.desafiovr.miniautorizador.repository.CartaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
}
