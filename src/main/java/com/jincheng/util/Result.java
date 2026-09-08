package com.jincheng.util;

import java.util.List;

public class Result<T> {
    private int code;
    private String msg;
    private int count;
    private List<T> data;
    private T data1;
    private String token;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
    public T getData1() { return data1; }
    public void setData1(T data1) { this.data1 = data1; }

    public static <T> Result<T> success(T payload) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg("操作成功");
        result.applyPayload(payload);
        return result;
    }

    public static <T> Result<T> success(String msg, T payload) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg(msg);
        result.applyPayload(payload);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMsg(msg);
        result.setCount(0);
        return result;
    }

    @SuppressWarnings("unchecked")
    private void applyPayload(T payload) {
        this.setData1(payload);
        if (payload instanceof List<?>) {
            List<T> list = (List<T>) payload;
            this.setData(list);
            this.setCount(list.size());
        } else {
            this.setCount(payload == null ? 0 : 1);
        }
    }
}

