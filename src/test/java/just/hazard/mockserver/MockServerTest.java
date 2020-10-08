package just.hazard.mockserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import just.hazard.mockserver.entity.Todo;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MockServerTest {


    private static ClientAndServer mockServer;

    @BeforeAll
    public void startMockServer() {
        mockServer = startClientAndServer(1080);
    }

    @AfterAll
    public void stopMockServer() {
        mockServer.stop();
    }

    @Test
    public void postTodo() throws JsonProcessingException {
        Todo todo = new Todo();
        todo.setTitle("justhis");
        todo.setDescription("show me the money");

        setMockApi(todo,HttpMethod.POST,"/todo",HttpStatusCode.CREATED_201,5);
        ResponseEntity<Todo> entity = postTodoRequest();
        Assertions.assertEquals("justhis",entity.getBody().getTitle());
        Assertions.assertEquals("show me the money", entity.getBody().getDescription());
        Assert.assertEquals(201,entity.getStatusCodeValue());
    }

    public void setMockApi(Object entity,
                           HttpMethod httpMethod,
                           String path,
                           HttpStatusCode httpStatusCode,
                           int delay
                           ) throws JsonProcessingException {
        mockServer
            .when(
                request()
                    .withMethod(httpMethod.toString())
                    .withPath(path)
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(serialize(entity))
            )
            .respond(
                    response()
                        .withStatusCode(httpStatusCode.code())
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(serialize(entity))
                        .withDelay(TimeUnit.SECONDS, delay)
            );
    }

    public ResponseEntity<Todo> postTodoRequest() {

        // given
        Todo todo = new Todo();
        todo.setTitle("justhis");
        todo.setDescription("show me the money");

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity("http://localhost:1080/todo", todo, Todo.class);
    }

    public <T> String serialize(T t) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(t);
    }
}
