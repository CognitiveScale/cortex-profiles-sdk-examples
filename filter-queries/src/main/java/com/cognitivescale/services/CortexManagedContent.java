package com.cognitivescale.services;

import com.cognitivescale.models.CortexContentTag;
import com.cognitivescale.models.CortexResponse;
import com.cognitivescale.utils.CortexHelper;
import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class CortexManagedContent extends CortexHelper implements ManagedContent {

    private static final String uriFormat = "/fabric/v%d/projects/%s/content/%s";

    public CortexManagedContent(String apiEndpoint, String token) {
        super(apiEndpoint, token, uriFormat);
    }

    public CortexContentTag upload(String projectKey, String contentKey, String contentType, InputStream inputStream) throws IOException, InterruptedException {
        HttpRequest httpRequest = builder(projectKey, contentKey)
                .setHeader("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> inputStream))
                .build();
        HttpResponse<String> send = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return new Gson().fromJson(send.body(), CortexContentTag.class);
    }

    @Override
    public CortexResponse delete(String projectKey, String contentKey) {
        return null;
    }

    public InputStream download(String projectKey, String contentKey) throws IOException, InterruptedException {
        HttpRequest request = builder(projectKey, contentKey).GET().build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        return response.body();
    }

    @Override
    public List<CortexContentTag> list(String projectKey, String filterKey) throws URISyntaxException, IOException, InterruptedException {
        NameValuePair filter = filterKey == null ? null : new BasicNameValuePair("filter", filterKey);
        HttpRequest httpRequest = withParameters(projectKey, filter).GET().build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(response.body(), contentListType);
    }

}
