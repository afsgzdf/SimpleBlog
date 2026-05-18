package com.serve.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data         //防止后端解析的数据前端看不懂
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<T>();
        result.code = 1;
        result.message = "success";
        result.data = data;

        return result;
    }

    public static Result success() {
        Result result = new Result();
        result.code = 1;
        result.message = "success";

        return result;
    }

    public static Result error(String message) {
        Result result = new Result();
        result.code = 0;
        result.message = message;

        return result;
    }
}
