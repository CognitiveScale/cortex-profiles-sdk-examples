package com.cognitivescale.utils;

import com.cognitivescale.models.CortexContentTag;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Optional;

public class CortexHelper {

    protected final URI apiEndpoint;
    protected final int version = 4;
    protected final String token;
    protected final HttpClient httpClient = HttpClient.newHttpClient();
    protected final Type contentListType = new TypeToken<ArrayList<CortexContentTag>>() {
    }.getType();
    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    protected final String uriFormat;

    public CortexHelper(String apiEndpoint, String token, String uriFormat) {
        this.apiEndpoint = URI.create(apiEndpoint);
        this.token = token;
        this.uriFormat = uriFormat;
    }

    public URI getUrl(String project, String key) {
        String contentKey = Optional.ofNullable(key).orElse("");
        return apiEndpoint.resolve(String.format(uriFormat, version, project, contentKey)).normalize();
    }


    protected HttpRequest.Builder builder(String project, String key) {
        return HttpRequest.newBuilder(getUrl(project, key))
                .setHeader("Authorization", String.format("Bearer %s", token));

    }

    protected HttpRequest.Builder withParameters(String project, NameValuePair... parameters) throws URISyntaxException {
        URI url = getUrl(project, null);
        return HttpRequest.newBuilder(
                        new URIBuilder(url).setParameters(parameters).build())
                .setHeader("Authorization", String.format("Bearer %s", token));
    }
}
