package com.jagrosh.jmusicbot.utils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.LoggerFactory;

import com.jagrosh.jmusicbot.utils.Http.SimpleRequest;

import okhttp3.OkHttpClient;

public class TextUtil {
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * This method will call all available paste services to attempt to upload the body, and take care of logging any
     * issues with those underlying paste services, callers only have to handle success or failure (the latter
     * represented by an empty Optional)
     *
     * @param body the content that should be uploaded to a paste service
     * @return the url of the uploaded paste, or null if there was an exception doing so. This is represented by the
     * Optional return type
     */
    public static CompletionStage<Optional<String>> postToPasteService(String body) {
        return postToWastebin(body)
            .thenApply(key -> Optional.of("https://wastebin.party/" + key))
            .exceptionally(t -> {
                LoggerFactory.getLogger("TextUtil").error("Could not post to wastebin", t);
                return Optional.empty();
            })
            .thenCompose(url -> {
                return CompletableFuture.completedFuture(url);
            });
    }

    private static CompletionStage<String> postToWastebin(String body) {
        return postToHasteBasedService("https://wastebin.party/documents", body,
            Optional.ofNullable("wastebinUser"), Optional.ofNullable(null));
    }

    private static CompletionStage<String> postToHasteBasedService(String baseUrl, String body,
        Optional<String> user, Optional<String> pass) {

        Http httpClient = new Http(Http.DEFAULT_BUILDER.newBuilder().build());
        SimpleRequest request = httpClient.post(baseUrl, body, "text/plain");
        if (user.isPresent() && pass.isPresent()) {
            request = request.basicAuth(user.get(), pass.get());
        }

        return request.enqueue()
            .asJson()
            .thenApply(json -> json.getString("key"));
    }

}