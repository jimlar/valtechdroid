package se.jimlar.intranet;

import android.util.Log;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class APIClient {
    private String username;
    private String password;
    private final APIResponseParser parser;

    public APIClient(String username, String password, APIResponseParser parser) {
        this.username = username;
        this.password = password;
        this.parser = parser;
    }

    public boolean authenticate() {
        DefaultHttpClient client = getHttpClient();
        HttpGet request = new HttpGet("https://intranet.valtech.se/api/employees/");
        try {
            HttpResponse response = client.execute(request);
            StatusLine status = response.getStatusLine();
            Log.i("API", "status.getStatusCode() = " + status.getStatusCode());
            Log.i("API", "status.getReasonPhrase() = " + status.getReasonPhrase());
            return status.getStatusCode() == 200;

        } catch (IOException e) {
            Log.w("API", "Could not authenticate", e);
            return false;
        }
    }

    public List<Employee> getEmployees() {
        DefaultHttpClient client = getHttpClient();
        HttpGet request = new HttpGet("https://intranet.valtech.se/api/employees/");
        String data = execteRequest(client, request);
        return parser.parseEmployees(data);
    }

    private DefaultHttpClient getHttpClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getCredentialsProvider().setCredentials(new AuthScope("intranet.valtech.se", 443),
                                                       new UsernamePasswordCredentials(username, password));
        return client;
    }

    private String execteRequest(DefaultHttpClient client, HttpUriRequest request) {
        try {
            HttpResponse response = client.execute(request);

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
            throw new RuntimeException("Problem communicating with API", e);
        }
    }

}