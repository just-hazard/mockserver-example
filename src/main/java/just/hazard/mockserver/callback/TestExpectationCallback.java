package just.hazard.mockserver.callback;

import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class TestExpectationCallback implements ExpectationCallback {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        if(httpRequest.getPath().getValue().endsWith("/callback")) {
            return httpResponse;
        } else {
            return HttpResponse.notFoundResponse();
        }
    }

    public static HttpResponse httpResponse = HttpResponse.response().withStatusCode(200);
}
