package com.serve.wrapper;

import com.serve.util.XssUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] cacheBody;
    private final String encoding;

    public XssHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 获取请求编码，默认UTF-8
        this.encoding = request.getCharacterEncoding() != null ?
        request.getCharacterEncoding() : "UTF-8";
        // 1. 读取原始请求体
        String rawBody = readRequestBody(request);
        // 2. 过滤请求体（移除HTML标签）
        String filterBody = XssUtil.xssClean(rawBody);
        // 3. 把过滤后的内容缓存到字节数组里
        this.cacheBody = filterBody.getBytes(encoding);
    }

    // 读取原始请求体的工具方法
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    //重写getInputStream，返回过滤后的缓存流
    @Override
    public ServletInputStream getInputStream() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(cacheBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

    //重写getReader，返回过滤后的BufferedReader
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), encoding));
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return XssUtil.xssClean(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] value = super.getParameterValues(name);
        if (value == null)
            return null;

        String[] filtered = new String[value.length];
        for (int i = 0; i < value.length; i++) {
            filtered[i] = XssUtil.xssClean(value[i]);
        }
        return filtered;
    }

    @Override
    public String getHeader(String name) {
        String header = super.getHeader(name);
        return XssUtil.xssClean(header);
    }
}
