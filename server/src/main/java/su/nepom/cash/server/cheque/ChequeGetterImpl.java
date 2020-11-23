package su.nepom.cash.server.cheque;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import su.nepom.cash.dto.ChequeDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;

/**
 * Получение содержимого чека из nalog.ru
 * <p>
 * Реализация взята из https://github.com/valiotti/get-receipts
 * Используется внутренний протокол (подсмотренный в официальном приложении)
 * <p>
 * Для работы необходим ИНН и пароль доступа к личному кабинету - задаются в настройках
 */
@SuppressWarnings("SpellCheckingInspection")
@Slf4j
@Service
class ChequeGetterImpl implements ChequeGetter {
    private static final String
            HOST = "irkkt-mobile.nalog.ru:8888",
            DEVICE_OS = "iOS",
            CLIENT_VERSION = "2.9.0",
            DEVICE_ID = "7C82010F-16CC-446B-8F66-FC4080C66521",
            ACCEPT = "*/*",
            USER_AGENT = "billchecker/2.9.0 (iPhone; iOS 13.6; Scale/2.00)",
            ACCEPT_LANGUAGE = "ru-RU;q=1, en-US;q=0.9",
            CLIENT_SECRET = "IyvrAbKt9h/8p6a7QPh8gpkXYQ4=";

    private final static DateTimeFormatter dateTimeFormatter =
            new DateTimeFormatterBuilder().appendPattern("y-MM-dd'T'HH:mm:ssxxx").toFormatter();

    private final String inn, password;
    private final ObjectMapper objectMapper;

    private final Retry retry = Retry.ofDefaults("ChequeGetterImpl");
    private final CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("ChequeGetterImpl");

    public ChequeGetterImpl(@Value("${nalog.INN:}") String inn, @Value("${nalog.password:}") String password, ObjectMapper objectMapper) {
        this.inn = inn;
        this.password = password;
        this.objectMapper = objectMapper;
    }

    @Override
    @SneakyThrows
    public ChequeDto getCheque(String qr) {
        if (inn.isBlank())
            throw new IllegalArgumentException("Сервис не настроен");

        // В circuitBreaker обернем всю цепочку сразу
        return CircuitBreaker.decorateCheckedFunction(circuitBreaker, this::getChequeImpl).apply(qr);
    }

    private ChequeDto getChequeImpl(String qr) {
        // а в Retry - каждый из вызовов
        var sessionId = Retry.decorateSupplier(retry, this::getSessionId).get();
        var ticketId = Retry.decorateSupplier(retry, () -> getTicketId(sessionId, qr)).get();
        var ticket = Retry.decorateSupplier(retry, () -> getTicket(sessionId, ticketId)).get();
        return parseTicket(ticket);
    }

    private HttpHeaders createHeaders(String sessionId) {
        var h = new HttpHeaders();
        h.set("Host", HOST);
        h.set("Accept", ACCEPT);
        h.set("Device-OS", DEVICE_OS);
        h.set("Device-Id", DEVICE_ID);
        h.set("clientVersion", CLIENT_VERSION);
        h.set("Accept-Language", ACCEPT_LANGUAGE);
        h.set("User-Agent", USER_AGENT);
        if (sessionId != null)
            h.set("sessionId", sessionId);
        return h;
    }

    private String getSessionId() {
        try {
            var request = new HttpEntity<>(
                    Map.of("inn", inn,
                            "client_secret", CLIENT_SECRET,
                            "password", password),
                    createHeaders(null));

            var rt = new RestTemplate();
            var response = rt.postForEntity(String.format("https://%s/v2/mobile/users/lkfl/auth", HOST), request, String.class);
            log.info("getSessionId: {}", response);

            return objectMapper.readTree(response.getBody()).path("sessionId").asText();
        } catch (Exception e) {
            throw new GetChequeException("getSession error: " + e.getMessage());
        }
    }

    private String getTicketId(String sessionId, String qr) {
        try {
            var request = new HttpEntity<>(
                    Map.of("qr", qr),
                    createHeaders(sessionId));

            var rt = new RestTemplate();
            var response = rt.postForEntity(String.format("https://%s/v2/ticket", HOST), request, String.class);
            log.info("getTicketId: {}", response);

            return objectMapper.readTree(response.getBody()).path("id").asText();
        } catch (Exception e) {
            throw new GetChequeException("getTicketId error: " + e.getMessage());
        }
    }

    private String getTicket(String sessionId, String ticketId) {
        try {
            var request = new HttpEntity<>(createHeaders(sessionId));
            var rt = new RestTemplate();
            var response = rt.exchange(String.format("https://%s/v2/tickets/%s", HOST, ticketId), HttpMethod.GET,
                    request, String.class);
            log.info("getTicket: {}", response.getBody());
            if (response.getStatusCode().isError())
                throw new GetChequeException("getTicket error: " + response.getStatusCode().toString());
            return response.getBody();
        } catch (Exception e) {
            throw new GetChequeException("getTicket error: " + e.getMessage());
        }
    }

    private ChequeDto parseTicket(String ticket) {
        try {
            var root = objectMapper.readTree(ticket);
            var cheque = new ChequeDto();

            var date = root.path("createdAt").asText();
            cheque.setTime(dateTimeFormatter.parse(date, Instant::from));

            var receiptNode = root.at("/ticket/document/receipt");
            cheque.setCash(BigDecimal.valueOf(receiptNode.path("cashTotalSum").asLong(), 2));
            cheque.setEcash(BigDecimal.valueOf(receiptNode.path("ecashTotalSum").asLong(), 2));
            cheque.setRetailPlace(receiptNode.path("retailPlace").asText(""));
            cheque.setRetailPlaceAddress(receiptNode.path("retailPlaceAddress").asText(""));
            receiptNode.path("items").elements().forEachRemaining(itemNode ->
                    cheque.addItem(itemNode.path("name").asText(), itemNode.path("quantity").asDouble(),
                            BigDecimal.valueOf(itemNode.path("sum").asLong(), 2)));

            return cheque;
        } catch (Exception e) {
            throw new GetChequeException("parseTicket error: " + e.getMessage());
        }
    }
}
