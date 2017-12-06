package se.anyro.bx;

import java.io.IOException;

@SuppressWarnings("serial")
public class HttpResponseException extends IOException {

    private int responseCode;

    public HttpResponseException(int errorCode, String description) {
        super(description);
        this.responseCode = errorCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
