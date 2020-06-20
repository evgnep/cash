package su.nepom.cash.server.remote.crud;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Вспомогательный класс для проверки ответов
 */
public class ResponseBodyMatchers {
    private HttpStatus status = HttpStatus.OK;

    public static ResponseBodyMatchers responseBody() {
        return new ResponseBodyMatchers();
    }

    public ResultMatcher containsObjectAsJson(Object expectedObject) {
        return mvcResult -> {
            assertThat(mvcResult.getResponse().getStatus()).describedAs("status: " + status).isEqualTo(status.value());

            String json = mvcResult.getResponse().getContentAsString();
            var expected = ObjectMapperConfig.MAPPER.writeValueAsString(expectedObject);
            assertThat(json).isEqualTo(expected);
        };
    }

    /**
     * Ответы с Page<X>
     */
    public ResultMatcher containsPageAsJson(List<?> expectedList) {
        return mvcResult -> {
            assertThat(mvcResult.getResponse().getStatus()).describedAs("status: " + status).isEqualTo(status.value());

            String json = mvcResult.getResponse().getContentAsString();

            var content = JsonPath.parse(json).read("$.content");
            var jsonContent = ObjectMapperConfig.MAPPER.writeValueAsString(content);

            var expected = ObjectMapperConfig.MAPPER.writeValueAsString(expectedList);
            assertThat(jsonContent).isEqualTo(expected);
        };
    }
}
