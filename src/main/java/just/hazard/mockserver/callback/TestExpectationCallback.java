package just.hazard.mockserver.callback;

import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.util.concurrent.TimeUnit;

public class TestExpectationCallback implements ExpectationResponseCallback {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        if(httpRequest.getPath().getValue().endsWith("/callback")) {
            return httpResponse;
        } else {
            return HttpResponse.notFoundResponse();
        }
    }

    private static final HttpResponse httpResponse = HttpResponse.response().withStatusCode(200).withDelay(TimeUnit.SECONDS,10);
}
