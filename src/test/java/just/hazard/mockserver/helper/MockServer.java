package just.hazard.mockserver.helper;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MockServer {

    private final String host = "http://localhost";
    private final int port = 1080;

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    ClientAndServer mockServer;

    @BeforeAll
    public void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(port);
    }

    @AfterAll
    public void stopMockServer() {
        mockServer.stop();
    }

    public RestTemplate getRestTemplate() {

        return restTemplateBuilder.build();
    }
}
