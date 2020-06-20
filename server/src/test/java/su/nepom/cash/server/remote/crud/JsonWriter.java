package su.nepom.cash.server.remote.crud;

import lombok.SneakyThrows;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Вспомогательный класс для rest-запросов с json объекта
 */
public class JsonWriter implements RequestPostProcessor {
    private final Object object;

    private JsonWriter(Object object) {
        this.object = object;
    }

    @SneakyThrows
    @Override
    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
        request.setContent(ObjectMapperConfig.MAPPER.writeValueAsBytes(object));
        request.setContentType("application/json");
        return request;
    }

    public static RequestPostProcessor json(Object object) {
        return new JsonWriter(object);
    }
}
