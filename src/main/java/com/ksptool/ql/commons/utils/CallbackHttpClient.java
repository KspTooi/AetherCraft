package com.ksptool.ql.commons.utils;

import com.ksptool.ql.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

@Slf4j
public class CallbackHttpClient {

    private final OkHttpClient httpClient;
    private Consumer<String> onResponse;
    private Consumer<Exception> onError;
    private Runnable onComplete;

    public CallbackHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CallbackHttpClient onResponse(Consumer<String> onResponse) {
        this.onResponse = onResponse;
        return this;
    }

    public CallbackHttpClient onError(Consumer<Exception> onError) {
        this.onError = onError;
        return this;
    }

    public CallbackHttpClient onComplete(Runnable onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    public void send(Request request) {
        Thread.startVirtualThread(() -> {
            try (Response response = this.httpClient.newCall(request).execute()) {
                
                if (!response.isSuccessful()) {
                    if (onError != null) {
                        onError.accept(new BizException("HTTP request failed: " + response.code() + " " + response.message()));
                    }
                    return;
                }

                if (response.body() == null) {
                    if (onError != null) {
                        onError.accept(new BizException("HTTP response body is null"));
                    }
                    return;
                }

                try (var reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (onResponse != null) {
                            onResponse.accept(line);
                        }
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            } catch (Exception e) {
                if (onError != null) {
                    onError.accept(e);
                }
            }
        });
    }

    public void send(Request request, Consumer<String> onResponse, Consumer<Throwable> onError, Runnable onComplete) {
        Thread.startVirtualThread(() -> {
            try (Response response = this.httpClient.newCall(request).execute()) {
                
                if (!response.isSuccessful()) {
                    onError.accept(new BizException("HTTP request failed: " + response.code() + " " + response.message()));
                    return;
                }

                if (response.body() == null) {
                    onError.accept(new BizException("HTTP response body is null"));
                    return;
                }

                try (var reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        onResponse.accept(line);
                    }
                    onComplete.run();
                }
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

    public OkHttpClient getClient() {
        return httpClient;
    }
}
