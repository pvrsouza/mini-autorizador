package br.com.desafiovr.miniautorizador.service.transacoes.processador;

import br.com.desafiovr.miniautorizador.exceptions.ValidacaoTransacaoException;
import br.com.desafiovr.miniautorizador.model.dto.input.TransacaoRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CadeiaValidacoesTransacaoImpl implements CadeiaValidacoesTransacao {
    private List<ValidacaoTransacao> validacaoTransacaos;
    private final ValidaSenhaTransacao validaSenhaTransacao;
    private final ValidaCartaoTransacao validaCartaoTransacao;
    private final ValidaSaldoTransacao validaSaldoTransacao;

    public CadeiaValidacoesTransacaoImpl(ValidaSenhaTransacao validaSenhaTransacao, ValidaCartaoTransacao validaCartaoTransacao, ValidaSaldoTransacao validaSaldoTransacao) {

        this.validaSenhaTransacao = validaSenhaTransacao;
        this.validaCartaoTransacao = validaCartaoTransacao;
        this.validaSaldoTransacao = validaSaldoTransacao;

        // Inicia a cadeia de validações. A ordem é importante
        this.validacaoTransacaos = List.of(
                this.validaCartaoTransacao,
                this.validaSenhaTransacao,
                this.validaSaldoTransacao
        );
    }

    @Override
    public void execute(TransacaoRequestDto transacao) throws ValidacaoTransacaoException {
        log.info("Iniciando validação de transação - cartão  {}", transacao);

        this.validacaoTransacaos
                .forEach(validacaoTransacao -> validacaoTransacao.validar(transacao));

        log.info("Validação concluída - Transação autorizada {}", transacao);
    }
}
