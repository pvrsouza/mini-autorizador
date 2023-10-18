# Instalação e Configuração
## Requisitos do sistema
- Docker
- Java 17
- MySql
- Maven

# Guia de Desenvolvimento
## Estrutura do projeto
- `src/main/java/br/com/desafiovr/miniautorizador/config`: Configuração de beans do projeto
- `src/main/java/br/com/desafiovr/miniautorizador/controllers`: Contém os controllers rest da aplicação
- `src/main/java/br/com/desafiovr/miniautorizador/enums`: Contém os Enums da aplicação.
- `src/main/java/br/com/desafiovr/miniautorizador/exceptions`: Contém as exceptions
- `src/main/java/br/com/desafiovr/miniautorizador/locker`: Contem implementação de lock distribuido
- `src/main/java/br/com/desafiovr/miniautorizador/model`: Contém as entities e DTOs ( input e output ) da aplicação.
- `src/main/java/br/com/desafiovr/miniautorizador/repository`: Contém os repository da aplicação para comunicação com banco de dados.
- `src/main/java/br/com/desafiovr/miniautorizador/service`: Contém os serviços com a lógica de negócio da aplicação.
- `src/main/java/br/com/desafiovr/miniautorizador/swagger`: Contém constates para centralizar valores do Swagger
- `src/main/resources/messages.properties`: Contém as mensagens de erro da aplicação

# Docker

## Comandos utilitários.
Executar os comandos no dirétorio raiz do projeto, onde o `docker-compose.yaml` está presente.
1. Disponibilizar somente o container do banco de dados
```bash
docker compose up postgres
```
2. Acessar o container de banco de dados para listar `databases` e `tables`
```bash
#caso o container não esteja rodando exetucar o comando
docker compose up

#listar os containers em execução
docker ps

#executar em modo interativo o shell do container que foi informado
docker exec -it <id-do-container> mysql -h 127.0.0.1 -P 3306 -u root

#lista os databases
SHOW DATABASES;

#lista os schemas
SHOW SCHEMAS;

#lista tabelas
show tables;

#conecta no schema
use miniautorizador;

```
3. Parar a execução dos containers
```bash
docker compose down
```

## docker-compose.yaml

O docker compose proposto no desafio teve um pequeno ajuste pra que fosse possível criar uma tabela no banco de dados.
Essa tabela é necessária para viabilizar a solução de Lock Distribuido

Trecho incluido:
```yaml
  mysql:
    #...configurações pré-existentes    
    volumes:
      - "./scripts/schema.sql:/docker-entrypoint-initdb.d/1.sql"
    command: --init-file=/docker-entrypoint-initdb.d/1.sql
```

# API
## Swagger
http://localhost:8080/swagger-ui/index.html

## Arquitetura
### Lock Distribuido

Para atender ao requisito de que garantir que 2 transações disparadas ao mesmo tempo não causem problemas relacionados à concorrência 
foi utilizado um conceito de Lock Distribuido onde, por intermedio de um banco dados, nesse caso o proprio MySql da aplicação, geramos e disponiilizamos bloqueios para que as transações sejam executadas de forma sequencial.

O principal desafio nesse ponto, foi garantir que a transação que não consguiu bloqueio, no momento em que aguardava afinalização de uma anterior, pudesse ter um tempo de espera e tentar novamente. Essa solução de retentativa pode ser encontrado aqui:

```java
//src/main/java/br/com/desafiovr/miniautorizador/locker/LockDistribuido.java
private static <T> T tryToGetLock(final Supplier<T> task,
final String lockKey,
final int tempoMaximoDeEsperaParaAdiquirirBloqueio) throws Exception {
final long tempoMaximoDeEsperaBloqueio = TimeUnit.SECONDS.toMillis(tempoMaximoDeEsperaParaAdiquirirBloqueio);

final long startTimestamp = System.currentTimeMillis();
        while (true) {
        log.info("Tentando pegar um bloqueio para a key '{}'", lockKey);
final T response = task.get();
        if (response != null) {
        return response;
        }
        log.info("Retentativa de obter bloqueio para a key  '{}'", lockKey);
        sleep(INTERVALOS_DE_RETENTETIVAS);

        if (System.currentTimeMillis() - startTimestamp > tempoMaximoDeEsperaBloqueio) {
        throw new Exception("Falha ao tentar adiquirir um bloqueio. Expirou o tempo máximo de espera para obter bloqueio: " + tempoMaximoDeEsperaBloqueio + " milliseconds");
        }
        }
        }
```

Esse recurso não pode ficar ad aeternum realizando tentativas, então por isso, temos a possibilidade de fazer ajustes finos no tempo máximo de espera para adiquirir o bloqueio. 

Essa configuração pode ser vista nos parâmetros do método `tryToGetLock` acima.


### Banco de dados
O desafio disponibilizou duas opções de base de dados para serem utilizadas. Uma relacional e outra não relacional.
Apesar de um dos requisitos informarem que as trasanções não precisam ser persisitdas, optei por utilizar o banco de dados relacional para armazenar as informações de cartão e saldo, visto que
num cenário real, em produção, o banco de dados relacional seria a melhor opção para armazenar essas informações de transações.

A principal caracteristica que me levou a decidir pelo banco relacional foi a Consistência ACID. Bancos de dados relacionais são conhecidos por fornecer garantias ACID (Atomicidade, Consistência, Isolamento e Durabilidade), tornando-os ideais para transações financeiras, onde a integridade dos dados é fundamental.
Neste momento, consigo enxergar como principal desvantagem na utilização do banco relaional a escalabilidade, visto que em cenários de alto volume, escalar bancos de dados relacionais pode ser mais complexo e caro do que sistemas NoSQL.

Falando um pouco sobre o banco NoSql, entendo que a principal desvantagem em utilizar seria complexidade de consulta, pois consultas complexas podem ser mais difíceis de expressar em sistemas NoSQL, especialmente se você precisar de operações de junção.

### Garantia de Unicidade de Cartão
Considerando um cenário distribuido, optei por utilizar um recurso de banco de dados para garantira unicidade do cartão.

Com a anoteção `@Column` é possível definir algumas propriedades para a coluna do banco de dados. Neste caso, utilizei a propriedade `unique = true` para garantir que o cartão seja único.
Internamente o Hibernate irá criar uma constraint no banco de dados para garantir a unicidade do cartão.

```java
@Column(name="numero_cartao", nullable = false, unique = true)
private String numeroCartao;

```

### Centralização de Mensagens de Erro
Afim de exercitar um pouco o uso de boas praticas, centralizei as mensagens de erro em um arquivo de properties, para evitar que as mensagens fiquem espalhadas pelo código.
E para facilitar a recuperação dessas mensagens no código, foi criado uma estrutura de serviço para aopiar nisso já contendo algums métodos auxiliares.

Classe auxiliar para as mensagens
`src/main/java/br/com/desafiovr/miniautorizador/service/MensagensServiceImpl.java`


### Testes de Integração

Para viabilizar os testes de integração com banco de dados, que atualmente é o único serviço de terceiro que temos, foi incluido no projeto um profile especifico que configura um banco de dados em memória chamado H2.
Para que o H2 seja utilizado, basta informar o profile `tests` ao executar os testes.

As configurações dos properties do profile `tests` estão no arquivo `src/test/resources/application-tests.properties`

Exemplo de código:

```java
@DataJpaTest
@ActiveProfiles("tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ComponentScan("br.com.desafiovr.miniautorizador.service")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartaoServiceImplIntegrationTest {
    ///toda implementação dos testes
}
```

É importante usar esse conjunto de anotações para que o Spring possa configurar o contexto de testes de integração corretamente.

- `@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)`:Ele informar ao springboot que o contexto sempre será reiniciado apos a execução de cada método do teste. Em cenários mais complexos esse recursos pode ser utilizado pra prevenir alguns problemas de execução dos testes de integração.
- `@ComponentScan("br.com.desafiovr.miniautorizador.service")`: Informa ao springboot que ele deve escanear os componentes da aplicação para que possa ser injetado no teste.
- `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`: Informa ao springboot que seu teste deve usar o H2 Database em vez de tentar substituir o banco de dados principal da aplicação
- `@DataJpaTest`: Informa ao springboot que o teste deve ser executado com o contexto de teste de integração.

### Utilização de IFS

Uma dos desafios do desafio era construir a solução inteira sem utilizar nenhum if. Consegui fazer isso até o momento em que tive que criar uma solução de Lock Distribuido. Não utilizar `if` neste ponto tratia uma complexidade muito grande apesar de ser possível.
SE consideramos a aplicação como um todo, acredito que o uso de `if` foi bem reduzido. E um dos pontos que me ajudou a reduzir o uso de `if` foi a utilização de um design-pattern chamada `Chain of Responsibility` e alguns outros recursos de Java 8+.

Exemplo de subistituição de `if` usando recurso `ifPresentOrElse` de Java
```java
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
```

No caso da cadeia de validações necessárias para definir uma transação como válida, ainda focado em remover `ifs` do fluxo, optei por usar um design-pattern chamado `Chain of Responsibility` que pode ser encontrado nas estruturas do que chamei de `processaor':


```java
// inferface comum aos validadores 
public interface ValidacaoTransacao {
  void validar(TransacaoRequestDto request) throws ValidacaoTransacaoException;
}
```

```java
// implementação que define a cadeia de execução dos validadores
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
```
 Além da remoção dos `ifs` acredito que essa abordagem facilita a manutenção do código, pois é possível adicionar novas validações sem alterar o código existente. O que facilita a manutenção e evolução do código.

A principal desvantagem que enxergo nessa abordagem é a complexidade de debugar o código, pois o fluxo de execução não é tão claro quanto um `if` ou `switch`. E também nesta abordagem
é necessário criar uma classe para cada validação, o que pode aumentar a complexidade do projeto.

Outra desvantagem é que essa abordagem não me permite desvio de fluxo. Caso a primeira validação falhe, as demais não serão executadas. Isso pode ser uma vantagem ou desvantagem dependendo do cenário. Acredito que para esse caso especifico encaixou muito bem.



## Referências
Aqui estão listados links em que foram utilizados como referência para o desenvolvimento do projeto.

- [Spring Tips: Distributed Locks with Spring Integration](https://spring.io/blog/2019/06/19/spring-tips-distributed-locks-with-spring-integration).
- [How To Implement a Spring Distributed Lock](https://tanzu.vmware.com/developer/guides/spring-integration-lock/).
- [Documentação de shell comandos de Mysql](https://dev.mysql.com/doc/mysql-shell/8.0/en/mysql-shell-commands.html)
- [Distributed Locks in Spring-boot Microservice Environment](https://medium.com/@anil.java.story/distributed-locks-in-spring-boot-microservice-environment-f11dad3cc378)
- [Design Pattern Chain of Responsibility in Java](https://refactoring.guru/design-patterns/chain-of-responsibility/java/example)

## Desafio proposto
### Mini autorizador

A VR processa todos os dias diversas transações de Vale Refeição e Vale Alimentação, entre outras.
De forma breve, as transações saem das maquininhas de cartão e chegam até uma de nossas aplicações, conhecida como *autorizador*, que realiza uma série de verificações e análises. Essas também são conhecidas como *regras de autorização*.

Ao final do processo, o autorizador toma uma decisão, aprovando ou não a transação:
* se aprovada, o valor da transação é debitado do saldo disponível do benefício, e informamos à maquininha que tudo ocorreu bem.
* senão, apenas informamos o que impede a transação de ser feita e o processo se encerra.

Sua tarefa será construir um *mini-autorizador*. Este será uma aplicação Spring Boot com interface totalmente REST que permita:

* a criação de cartões (todo cartão deverá ser criado com um saldo inicial de R$500,00)
* a obtenção de saldo do cartão
* a autorização de transações realizadas usando os cartões previamente criados como meio de pagamento

### Regras de autorização a serem implementadas

Uma transação pode ser autorizada se:
* o cartão existir
* a senha do cartão for a correta
* o cartão possuir saldo disponível

Caso uma dessas regras não ser atendida, a transação não será autorizada.

### Demais instruções

O projeto contém um docker-compose.yml com 1 banco de dados relacional e outro não relacional.
Sinta-se à vontade para utilizar um deles. Se quiser, pode deixar comentado o banco que não for utilizar, mas não altere o que foi declarado para o banco que você selecionou.

Não é necessário persistir a transação. Mas é necessário persistir o cartão criado e alterar o saldo do cartão caso uma transação ser autorizada pelo sistema.

Serão analisados o estilo e a qualidade do seu código, bem como as técnicas utilizadas para sua escrita. Ficaremos felizes também se você utilizar testes automatizados como ferramenta auxiliar de criação da solução.

Também, na avaliação da sua solução, serão realizados os seguintes testes, nesta ordem:

OK - * criação de um cartão
OK - * verificação do saldo do cartão recém-criado
* realização de diversas transações, verificando-se o saldo em seguida, até que o sistema retorne informação de saldo insuficiente
* realização de uma transação com senha inválida
* realização de uma transação com cartão inexistente

Esses testes serão realizados:
* rodando o docker-compose enviado para você
* rodando a aplicação

Para isso, é importante que os contratos abaixo sejam respeitados:

### Contratos dos serviços

### Criar novo cartão
```
Method: POST
URL: http://localhost:8080/cartoes
Body (json):
{
    "numeroCartao": "6549873025634501",
    "senha": "1234"
}
```
#### Possíveis respostas:
```
Criação com sucesso:
   Status Code: 201
   Body (json):
   {
      "senha": "1234",
      "numeroCartao": "6549873025634501"
   } 
-----------------------------------------
Caso o cartão já exista:
   Status Code: 422
   Body (json):
   {
      "senha": "1234",
      "numeroCartao": "6549873025634501"
   } 
```

### Obter saldo do Cartão
```
Method: GET
URL: http://localhost:8080/cartoes/{numeroCartao} , onde {numeroCartao} é o número do cartão que se deseja consultar
```

#### Possíveis respostas:
```
Obtenção com sucesso:
   Status Code: 200
   Body: 495.15 
-----------------------------------------
Caso o cartão não exista:
   Status Code: 404 
   Sem Body
```

### Realizar uma Transação
```
Method: POST
URL: http://localhost:8080/transacoes
Body (json):
{
    "numeroCartao": "6549873025634501",
    "senhaCartao": "1234",
    "valor": 10.00
}
```

#### Possíveis respostas:
```
Transação realizada com sucesso:
   Status Code: 201
   Body: OK 
-----------------------------------------
Caso alguma regra de autorização tenha barrado a mesma:
   Status Code: 422 
   Body: SALDO_INSUFICIENTE|SENHA_INVALIDA|CARTAO_INEXISTENTE (dependendo da regra que impediu a autorização)
```

### Desafios (não obrigatórios):
* é possível construir a solução inteira sem utilizar nenhum if. Só não pode usar *break* e *continue*!
* como garantir que 2 transações disparadas ao mesmo tempo não causem problemas relacionados à concorrência?
  Exemplo: dado que um cartão possua R$10.00 de saldo. Se fizermos 2 transações de R$10.00 ao mesmo tempo, em instâncias diferentes da aplicação, como o sistema deverá se comportar?

