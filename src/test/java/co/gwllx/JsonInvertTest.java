package co.gwllx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.gwllx.jackson.invert.InvertModule;
import co.gwllx.jackson.invert.annotation.JsonInvert;

public class JsonInvertTest {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new InvertModule());

    private static record Wrapper(@JsonInvert List<Person> people) {}
    private static record Person(String name, int age) {}

    @Test
    public void testJsonInvert() throws IOException {
        String input = """
        {
          "people": {
            "name": ["Bill", "Bob"],
            "age": [20, 30]
          }
        }
        """;

        var data = MAPPER.readValue(input, Wrapper.class);

        assertEquals(2, data.people().size());
        assertEquals("Bill", data.people().get(0).name());
        assertEquals("Bob", data.people().get(1).name());
        assertEquals(20, data.people().get(0).age());
        assertEquals(30, data.people().get(1).age());
    }
}
