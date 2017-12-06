package se.anyro.bx;

/**
 * Error returned in the JSON response from BX. The error code is normally 200 and you need to look at the description
 * field to see what actually went wrong.
 */
@SuppressWarnings("serial")
public class BxApiException extends HttpResponseException {

    public BxApiException(int errorCode, String description) {
        super(errorCode, description);
    }
}
