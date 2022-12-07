package com.cognitivescale.services;

import com.cognitivescale.models.CortexContentTag;
import com.cognitivescale.models.CortexResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

public interface ManagedContent {
    CortexContentTag upload(String projectKey, String contentKey, String contentType, InputStream inputStream) throws IOException, InterruptedException;
    InputStream download(String projectKey, String contentKey) throws IOException, InterruptedException;
    List<CortexContentTag> list(String projectKey, String filterKey) throws URISyntaxException, IOException, InterruptedException;
    CortexResponse delete(String projectKey, String contentKey);
}
