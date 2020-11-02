package com.gateway.client;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import static org.asynchttpclient.Dsl.asyncHttpClient;


/**
 * @author lw
 */
public class Client {

    static private AsyncHttpClient asyncHttpClient = asyncHttpClient();

    static public byte[] getResponse(String url) throws ExecutionException, InterruptedException {
        ListenableFuture<Response> responseFuture = asyncHttpClient.prepareGet(url).execute();
        Response response = responseFuture.get();

        Charset charset = Charset.forName("ASCII");
        return response.getResponseBody().toString().getBytes(charset);
    }
}
