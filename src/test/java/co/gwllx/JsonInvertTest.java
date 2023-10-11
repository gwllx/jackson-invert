package co.gwllx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.gwllx.jackson.invert.InvertModule;
import co.gwllx.jackson.invert.annotation.JsonInvert;

public class JsonInvertTest {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new InvertModule());

    private static record ArrayWrapper(@JsonInvert Person[] people) {}
    private static record ListWrapper(@JsonInvert List<Person> people) {}
    private static record Person(String name, int age) {}

    @Test
    public void jsonInvertSuccess() throws IOException {
        var input = """
        {
          "people": {
            "name": ["Bill", "Bob"],
            "age": [20, 30]
          }
        }
        """;

        var listWrapper = MAPPER.readValue(input, ListWrapper.class);
        assertEquals(2, listWrapper.people().size());
        assertEquals("Bill", listWrapper.people().get(0).name());
        assertEquals("Bob", listWrapper.people().get(1).name());
        assertEquals(20, listWrapper.people().get(0).age());
        assertEquals(30, listWrapper.people().get(1).age());

        var arrayWrapper = MAPPER.readValue(input, ArrayWrapper.class);
        assertEquals(2, arrayWrapper.people().length);
        assertEquals("Bill", arrayWrapper.people()[0].name());
        assertEquals("Bob", arrayWrapper.people()[1].name());
        assertEquals(20, arrayWrapper.people()[0].age());
        assertEquals(30, arrayWrapper.people()[1].age());
    }

    @Test
    public void unevenArraySize() throws IOException {
        var input = """
        {
          "people": {
            "name": ["Bill", "Bob"],
            "age": [20]
          }
        }
        """;

        var listWrapper = MAPPER.readValue(input, ListWrapper.class);
        assertEquals(2, listWrapper.people().size());
        assertEquals("Bill", listWrapper.people().get(0).name());
        assertEquals("Bob", listWrapper.people().get(1).name());
        assertEquals(20, listWrapper.people().get(0).age());
        assertEquals(0, listWrapper.people().get(1).age());
    }

    @Test
    public void nonObjectJsonInvertProperty() {
        var input = """
        { "people": [] }
        """;

        var ex = assertThrowsExactly(JsonMappingException.class,
                () -> MAPPER.readValue(input, ListWrapper.class));
        assertTrue(ex.getMessage().startsWith("@JsonInvert property must be an object"));
    }

    @Test
    public void nonArrayJsonInvertField() {
        var input = """
        {
          "people": {
            "name": "Bill"
          }
        }
        """;

        var ex = assertThrowsExactly(JsonMappingException.class,
                () -> MAPPER.readValue(input, ListWrapper.class));
        assertTrue(ex.getMessage().startsWith("Field of a @JsonInvert property must be an array"));
    }
}
