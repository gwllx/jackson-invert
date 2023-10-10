# Jackson Invert

A Jackson module to invert JSON object-of-arrays into Java array-of-objects.

## Example

Given the JSON input:

```json
{
  "people": {
    "name": ["Bill", "Bob"],
    "age": [20, 30]
  }
}
```

The ``@JsonInvert`` annotation can be used to deserialize the following model:

```java
public record Wrapper(@JsonInvert List<Person> people) {}
public record Person(String name, int age) {}
```

## Usage

The ``InvertModule`` should be registered with a Jackson ``ObjectMapper``:

```java
var objectMapper = new ObjectMapper().registerModule(new InvertModule());
```
