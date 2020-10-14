package just.hazard.mockserver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderClassName = "TodoBuilder", toBuilder = true)
@JsonDeserialize(builder = Todo.TodoBuilder.class)
public class Todo {

    private Long id;
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
}
