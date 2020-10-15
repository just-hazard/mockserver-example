package just.hazard.mockserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import just.hazard.mockserver.entity.Todo;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpClassCallback.callback;
import static org.mockserver.model.HttpOverrideForwardedRequest.forwardOverriddenRequest;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MockServerTest {


    private static ClientAndServer mockServer;

    // TestInstance 의존 Annotation 기본적으로 단위 테스트 기반으로 데이터를
    // 새로 구성해야 하지만 Mockserver를 매번 올렸다 내리는 작업 소요 Resource가 많이 들어 라이프사이클 perClass로 지정
    @BeforeAll
    public void startMockServer() {
        mockServer = startClientAndServer(1080);
    }

    @AfterAll
    public void stopMockServer() {
        mockServer.stop();
    }

    @Test
    @DisplayName("TODO 등록 샘플 및 검증")
    void whenPostRequestMockServer_thenServerReceived() throws JsonProcessingException {
        // 샘플 데이터
        Todo todo = getTodo();
        // Mock API 생성
        createResponseMockApi(todo,HttpMethod.POST,"/todo",HttpStatusCode.CREATED_201,1);

        // http 요청 (restTemplate)
        ResponseEntity<Todo> result = postTodoRequest();
        Optional<Todo> responseBody = Optional.ofNullable(result.getBody());
        if(responseBody.isPresent())
        {
            assertEquals("justhis",responseBody.get().getTitle());
            assertEquals("show me the money", responseBody.get().getDescription());
        }
        assertEquals(201,result.getStatusCodeValue());
        // 검증 메서드
        verifyPostRequest();
    }

    @Test
    @DisplayName("포워드 샘플 및 검증")
    public void whenGetRequest_ThenForward() throws JsonProcessingException {
        // 샘플 데이터
        Todo todo = getTodo();
        // Mock API 생성
        createResponseMockApi(todo,HttpMethod.POST,"/todo",HttpStatusCode.CREATED_201,1);
        createForwardMockApi();
        String response = getTodoRequest();
        assertEquals("YeRin",response);
        verifyGetRequest();
    }

    @Test
    public void whenCallbackRequest_ThenCallbackMethodCalled(){
        createExpectationForCallBack();
        HttpResponse response= hitTheServerWithGetRequest("/callback");
        assertEquals(200,response.getStatusLine().getStatusCode());
    }

    private void verifyPostRequest() throws JsonProcessingException {

        Todo todo = getTodo();

        new MockServerClient("localhost", 1080).verify(
                request()
                        .withMethod("POST")
                        .withPath("/todo")
                        .withBody(serialize(todo))
        );
    }
    private void verifyGetRequest() {
        new MockServerClient("localhost", 1080).verify(
                request()
                        .withMethod("GET")
                        .withPath("/todo")
        );
    }

    private HttpResponse hitTheServerWithGetRequest(String page) {
        String url = "http://localhost:1080/"+page;
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response=null;
        HttpGet get = new HttpGet(url);
        try {
            response=client.execute(get);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    private void createForwardMockApi(){
        new MockServerClient("localhost", 1080)
                .when(
                        request()
                                .withMethod(HttpMethod.GET.name())
                                .withPath("/todo")
                )
                .forward(
                        forwardOverriddenRequest(
                                request()
                                    .withPath("/todo")
                                    .withMethod(HttpMethod.POST.name()),
                                response()
                                    .withBody("YeRin")
                        )
                );
    }

    private void createExpectationForCallBack(){
        mockServer
                .when(
                        request()
                                .withPath("/callback")
                )
                .respond(
                        callback()
                                .withCallbackClass("just.hazard.mockserver.callback.TestExpectationCallback")
                );
    }

    public void createResponseMockApi(Object entity,
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
        Todo todo = getTodo();

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity("http://localhost:1080/todo", todo, Todo.class);
    }

    public String getTodoRequest() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject("http://localhost:1080/todo", String.class);
    }

    public <T> String serialize(T t) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(t);
    }

    private Todo getTodo() {
        return Todo.builder()
                .title("justhis")
                .description("show me the money")
                .build();
    }
}
