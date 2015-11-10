package org.lobby.web.controller.response;

/**
 * @author Alexander Litus
 */
public class SimpleResponse<T> {

    private final T result;

    public static <T> SimpleResponse<T> create(T result){
        return new SimpleResponse<>(result);
    }

    public SimpleResponse(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }
}
