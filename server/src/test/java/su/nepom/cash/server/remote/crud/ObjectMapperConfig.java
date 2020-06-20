package su.nepom.cash.server.remote.crud;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;

/**
 * Настройка ObjectMapper для тестов
 */
public class ObjectMapperConfig {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        var module = new SimpleModule();
        module.addSerializer(new InstantSerializer());
        MAPPER.registerModule(module);
    }

    private static class InstantSerializer extends StdSerializer<Instant> {
        public InstantSerializer() {
            super(Instant.class);
        }

        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }
}
