package com.serve.client;

import com.serve.properties.DeepSeekProperties;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class AIRemoteClient {

    private final OkHttpClient okHttpClient;
    private final DeepSeekProperties deepSeekProperties;

    public InputStream getAIMsgStreamForJson(RequestBody requestBody) throws IOException {
        //拼接密钥
        String authorization = "Bearer " + deepSeekProperties.getSecretKey();
        Request request = new Request.Builder()
                .url(deepSeekProperties.getUrl())
                .method("POST", requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .addHeader("Authorization", authorization)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("请求失败: " + response);
        }
        return response.body().byteStream();
    }
}
