package com.serve.service;

import com.serve.dto.RequestAIDTO;

import java.util.function.Consumer;

public interface AIRemoteService {

    void remoteRequestAIStream(RequestAIDTO requestAIDTO, Consumer<String> consumer) throws Exception;
}
