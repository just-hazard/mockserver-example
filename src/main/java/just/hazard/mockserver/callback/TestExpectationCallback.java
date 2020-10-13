package just.hazard.mockserver.callback;

import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.mock.action.ExpectationForwardAndResponseCallback;
import org.mockserver.mock.action.ExpectationForwardCallback;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class TestExpectationCallback implements ExpectationResponseCallback {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        if(httpRequest.getPath().getValue().endsWith("/callback")) {
            try {
                Thread.sleep(3000);
                System.out.println("3초 대기");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return httpResponse;
        } else {
            return HttpResponse.notFoundResponse();
        }
    }

    public static HttpResponse httpResponse = HttpResponse.response().withStatusCode(200);
}
