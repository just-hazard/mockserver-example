package just.hazard.mockserver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder(builderClassName = "TodoBuilder", toBuilder = true)
@JsonDeserialize(builder = Todo.TodoBuilder.class)
public class Todo {

    private final Long id;

    private final String title;

    private final String description;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TodoBuilder {}
}
