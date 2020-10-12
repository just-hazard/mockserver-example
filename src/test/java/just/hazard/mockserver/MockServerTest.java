package just.hazard.mockserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import just.hazard.mockserver.entity.Todo;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpClassCallback;
import org.mockserver.model.HttpForward;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.verify.VerificationTimes;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpClassCallback.callback;
import static org.mockserver.model.HttpForward.forward;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;
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
        assertEquals("justhis",entity.getBody().getTitle());
        assertEquals("show me the money", entity.getBody().getDescription());
        Assert.assertEquals(201,entity.getStatusCodeValue());
    }

    @Test
    public void whenPostRequestMockServer_thenServerReceived(){
        createExpectationForInvalidAuth();
        hitTheServerWithPostRequest();
        verifyPostRequest();
    }

    @Test
    public void whenPostRequestForInvalidAuth_then401Received(){
        createExpectationForInvalidAuth();
        org.apache.http.HttpResponse response = hitTheServerWithPostRequest();
        assertEquals(401, response.getStatusLine().getStatusCode());
    }

    @Test
    public void whenGetRequest_ThenForward(){
        createExpectationForForward();
        hitTheServerWithGetRequest("index.html");
        verifyGetRequest();

    }

    @Test
    public void whenCallbackRequest_ThenCallbackMethodCalled(){
        createExpectationForCallBack();
        org.apache.http.HttpResponse response= hitTheServerWithGetRequest("/callback");
        assertEquals(200,response.getStatusLine().getStatusCode());
    }

    private void verifyPostRequest() {
        new MockServerClient("localhost", 1080).verify(
                request()
                        .withMethod("POST")
                        .withPath("/validate")
                        .withBody(exact("{username: 'foo', password: 'bar'}")),
                VerificationTimes.exactly(1)
        );
    }
    private void verifyGetRequest() {
        new MockServerClient("localhost", 1080).verify(
                request()
                        .withMethod("GET")
                        .withPath("/index.html"),
                VerificationTimes.exactly(1)
        );
    }

    private org.apache.http.HttpResponse hitTheServerWithPostRequest() {
        String url = "http://127.0.0.1:1080/validate";
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-type", "application/json");
        org.apache.http.HttpResponse response=null;

        try {
            StringEntity stringEntity = new StringEntity("{username: 'foo', password: 'bar'}");
            post.getRequestLine();
            post.setEntity(stringEntity);
            response=client.execute(post);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private org.apache.http.HttpResponse hitTheServerWithGetRequest(String page) {
        String url = "http://127.0.0.1:1080/"+page;
        HttpClient client = HttpClientBuilder.create().build();
        org.apache.http.HttpResponse response=null;
        HttpGet get = new HttpGet(url);
        try {
            response=client.execute(get);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    private void createExpectationForInvalidAuth() {
        new MockServerClient("127.0.0.1", 1080)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/validate")
                                .withHeader("\"Content-type\", \"application/json\"")
                                .withBody(exact("{username: 'foo', password: 'bar'}")),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(401)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400")
                                )
                                .withBody("{ message: 'incorrect username and password combination' }")
                                .withDelay(TimeUnit.SECONDS,1)
                );
    }

    private void createExpectationForForward(){
        new MockServerClient("127.0.0.1", 1080)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/index.html"),
                        exactly(1)
                )
                .forward(
                        forward()
                                .withHost("www.mock-server.com")
                                .withPort(80)
                                .withScheme(HttpForward.Scheme.HTTP)
                );
    }

    private void createExpectationForCallBack(){
        mockServer
                .when(
                        request()
                                .withPath("/callback")
                )
                .callback(
                        callback()
                                .withCallbackClass("just.hazard.mockserver.callback.TestExpectationCallback")
                );
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
