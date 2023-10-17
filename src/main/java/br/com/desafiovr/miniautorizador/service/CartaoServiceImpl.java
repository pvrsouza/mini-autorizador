package br.com.desafiovr.miniautorizador.service;

import br.com.desafiovr.miniautorizador.model.dto.input.CartaoRequestDto;
import br.com.desafiovr.miniautorizador.model.dto.output.CartaoResponseDto;
import br.com.desafiovr.miniautorizador.model.entity.Cartao;
import br.com.desafiovr.miniautorizador.repository.CartaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CartaoServiceImpl implements CartaoService{

    private final CartaoRepository repository;

    public CartaoServiceImpl(CartaoRepository repository) {
        this.repository = repository;
    }
    @Override
    public CartaoResponseDto create(CartaoRequestDto cartaoRequest) {
        try{
            Cartao cartao = cartaoRequest.toEntity();

            Cartao savedCartao = this.repository.save(cartao);
            log.info("Cartão salvo com sucesso. Cartão [{}]", cartao);

            return savedCartao.toResponseDto();
        }catch (Exception e){
            log.error("Erro ao salvar cartão", e);
            throw  e;
        }

    }
}
