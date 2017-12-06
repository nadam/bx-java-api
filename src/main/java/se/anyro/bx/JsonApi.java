package se.anyro.bx;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Simple base class for calling APIs that return JSON data.
 */
public class JsonApi {

    private final int CONNECTION_TIMEOUT;
    private final int READ_TIMEOUT;
    private final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd' 'HH:mm:ss").create();
    private final JsonParser PARSER = new JsonParser();
    
    /**
     * Constructor for the public API only. Accessible without api key and api secret.
     * 
     * @param connectionTimeout
     *            connection timeout in milliseconds
     * @param readTimeout
     *            timeout for each read in milliseconds
     */
    protected JsonApi(int connectionTimeout, int readTimeout) {
        CONNECTION_TIMEOUT = connectionTimeout;
        READ_TIMEOUT = readTimeout;
    }

    /**
     * Calls an API method and returns the json result as a pojo.
     * 
     * @param url
     *            the full URL of the method
     * @param responseClass
     *            the Java class corresponding to the JSON response
     * @return the json result as a pojo
     * @throws IOException
     */
    protected <T> T callMethod(String url, Class<T> responseClass) throws IOException {

        HttpURLConnection con = createConnectionForGet(url);

        try (Reader reader = new InputStreamReader(con.getInputStream())) {
            JsonObject response = (JsonObject) PARSER.parse(reader);
            checkErrorResponse(con, response);
            return GSON.fromJson(response, responseClass);
        } finally {
            closeInputStream(con);
        }
    }

    /**
     * Calls an API method using POST and returns the json result as a pojo.
     * 
     * @param url
     *            the full URL of the method
     * @param parameters
     *            the parameters to be posted
     * @param responseClass
     *            the Java class corresponding to the JSON response
     * @return the json result as a pojo
     * @throws IOException
     */
    protected <T> T callMethod(String url, String parameters, Class<T> responseClass) throws IOException {

        HttpURLConnection con = createConnectionForPost(url);

        try (OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(parameters);
        }

        try (Reader reader = new InputStreamReader(con.getInputStream())) {
            JsonObject response = (JsonObject) PARSER.parse(reader);
            checkErrorResponse(con, response);
            return GSON.fromJson(response, responseClass);
        } finally {
            closeInputStream(con);
        }
    }

    /**
     * Override this method to set request properties etc.
     */
    protected void setConnectionProperties(HttpURLConnection con) {
    }

    /**
     * Basic error handling. Override this to look for errors in the response object.
     */
    protected void checkErrorResponse(HttpURLConnection con, JsonObject response) throws IOException {
        int responseCode = con.getResponseCode();
        if (responseCode >= 300) {
            throw new HttpResponseException(responseCode, con.getResponseMessage());
        }
    }

    private HttpURLConnection createConnectionForGet(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(CONNECTION_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        setConnectionProperties(con);
        con.connect();
        return con;
    }

    private HttpURLConnection createConnectionForPost(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("charset", "utf-8");
        con.setConnectTimeout(CONNECTION_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        con.setUseCaches(false);
        setConnectionProperties(con);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        return con;
    }

    private void closeInputStream(HttpURLConnection con) {
        try {
            // Close to let the connection be reused
            con.getInputStream().close();
        } catch (IOException e) {
            // Ignore
        }
    }
}