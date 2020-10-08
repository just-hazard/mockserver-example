package just.hazard.mockserver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Todo {

    private Long id;
    private String title;
    private String description;
}
