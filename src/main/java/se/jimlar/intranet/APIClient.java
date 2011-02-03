package se.jimlar.intranet;

import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import se.jimlar.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class APIClient {
    public static final Logger LOG = new Logger(APIClient.class);

    private final APIResponseParser parser;
    private final DefaultHttpClient httpClient;

    public APIClient(String username, String password, APIResponseParser parser) {
        this.parser = parser;
        httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(new AuthScope("intranet.valtech.se", 443),
                                                       new UsernamePasswordCredentials(username, password));
    }

    public boolean authenticate() {
        HttpGet request = new HttpGet("https://intranet.valtech.se/api/employees/");
        try {
            HttpResponse response = httpClient.execute(request);
            StatusLine status = response.getStatusLine();
            LOG.info("status.getStatusCode() = " + status.getStatusCode());
            LOG.info("status.getReasonPhrase() = " + status.getReasonPhrase());
            return status.getStatusCode() == 200;

        } catch (IOException e) {
            LOG.warn("Could not authenticate", e);
            return false;
        }
    }

    public List<Employee> getEmployees() {
        HttpGet request = new HttpGet("https://intranet.valtech.se/api/employees/");
        String data = execteRequest(request);
        return parser.parseEmployees(data);
    }

    public void download(String path, ByteArrayOutputStream out) throws IOException {
        HttpGet request = new HttpGet("https://intranet.valtech.se" + path);
        LOG.debug("Downloading " + request.getURI());
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            response.getEntity().consumeContent();
            LOG.warn("Could not download path " + path + ", got status code " + response.getStatusLine().getStatusCode());
            return;
        }
        response.getEntity().writeTo(out);
    }

    private String execteRequest(HttpUriRequest request) {
        try {
            HttpResponse response = httpClient.execute(request);

            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) {
                throw new RuntimeException("Invalid response from server: " + status.toString());
            }

            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();

            int readBytes;
            byte[] sBuffer = new byte[512];
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }

            return new String(content.toByteArray());
        } catch (IOException e) {
            LOG.warn("Problem communicating with API", e);
            throw new RuntimeException("Problem communicating with API", e);
        }
    }
}
