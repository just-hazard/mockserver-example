package just.hazard.mockserver.exception;


import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import static org.mockserver.model.HttpResponse.notFoundResponse;
import static org.mockserver.model.HttpResponse.response;

public class ExceptionHandler implements ExpectationCallback {
    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        if(httpRequest.getPath().getValue().endsWith("/callback")) {
            return httpResponse;
        } else {
            return notFoundResponse();
        }
    }

    public static HttpResponse httpResponse = response().withStatusCode(200);
}
