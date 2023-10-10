package co.gwllx.jackson.invert;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class InvertDeserializer extends JsonDeserializer<Object>
        implements ContextualDeserializer {

    private static class ArrayValueBuffer {
        private String name;
        private Deque<TokenBuffer> values;

        public ArrayValueBuffer(String name, Deque<TokenBuffer> values) {
            this.name = name;
            this.values = values;
        }

        public String name() { return name; }
        public boolean hasValue() { return values.peek() != null; }
        public TokenBuffer nextValue() { return hasValue() ? values.pop() : null; }
    }

    private JavaType type;

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context,
            BeanProperty property) throws JsonMappingException {
        this.type = property.getType();

        if (!type.isContainerType()) {
            throw new JsonMappingException(context.getParser(),
                    "@JsonInvert requires a container type");
        }

        return this;
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JacksonException {
        if (!parser.isExpectedStartObjectToken()) {
            throw new JsonMappingException(parser, "Expected start of object");
        }

        var bufferedParser = rewriteInput(parser, context);
        return context.readValue(bufferedParser, type);
    }

    private JsonParser rewriteInput(JsonParser parser, DeserializationContext context)
            throws IOException {
        var fields = getArrayFieldValues(parser, context);
        var output = new TokenBuffer(parser.getCodec(), false);

        output.writeStartArray();
        writeObjects(output, fields);
        output.writeEndArray();

        return output.asParserOnFirstToken();
    }

    private void writeObjects(TokenBuffer output, List<ArrayValueBuffer> fields)
            throws IOException {
        while (anyHasValue(fields)) {
            output.writeStartObject();

            for (var field : fields) {
                var value = field.nextValue();

                if (value != null) {
                    output.writeFieldName(field.name());
                    output.append(value);
                }
            }

            output.writeEndObject();
        }
    }

    private boolean anyHasValue(List<ArrayValueBuffer> fields) {
        for (var field : fields) {
            if (field.hasValue()) { return true; }
        }

        return false;
    }

    private List<ArrayValueBuffer> getArrayFieldValues(JsonParser parser,
            DeserializationContext context) throws IOException {
        var buffers = new ArrayList<ArrayValueBuffer>();

        var token = parser.nextToken();
        while (token == JsonToken.FIELD_NAME) {
            var name = parser.currentName();
            parser.nextToken(); // Skip FIELD_NAME

            var fieldBuffer = context.bufferAsCopyOfValue(parser);
            if (!JsonToken.START_ARRAY.equals(fieldBuffer.firstToken())) {
                throw new JsonMappingException(parser, "Expected start of array");
            }

            var fieldParser = fieldBuffer.asParserOnFirstToken();
            fieldParser.nextToken(); // Skip START_ARRAY
            
            var values = new ArrayDeque<TokenBuffer>();
            while (!fieldParser.hasTokenId(JsonTokenId.ID_END_ARRAY)) {
                values.add(context.bufferAsCopyOfValue(fieldParser));
                fieldParser.nextValue();
            }

            buffers.add(new ArrayValueBuffer(name, values));
            token = parser.nextToken();
        }

        return buffers;
    }

}
