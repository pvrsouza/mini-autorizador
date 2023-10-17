package br.com.desafiovr.miniautorizador.service;

public interface MensagensService {
    String getErrorMessage(String key);

    String getErrorMessage(String key, String... args);

    String getNullPointerErrorMessage();
}
