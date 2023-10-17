package br.com.desafiovr.miniautorizador.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class MensagensServiceImpl implements MensagensService{

    private final MessageSource messageSource;

    public MensagensServiceImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getErrorMessage(String key) {
        Locale locale = Locale.getDefault();
        return messageSource.getMessage(key, null, locale);
    }

    public String getErrorMessage(String key, String... args) {
        Locale locale = Locale.getDefault();
        return messageSource.getMessage(key, args, locale);
    }

    @Override
    public String getNullPointerErrorMessage() {
        Locale locale = Locale.getDefault();
        return messageSource.getMessage("error.nullpointer", null, locale);
    }
}
