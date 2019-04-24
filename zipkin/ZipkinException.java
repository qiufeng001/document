package com.wayeal.cloud.zipkin;

/**
 * 自定义异常类
 *
 * @author z.h
 */
public class ZipkinException extends RuntimeException {

    public ZipkinException(String message) {
        super(message);
    }

    public ZipkinException(Exception e) {
        super(e);
    }

}
