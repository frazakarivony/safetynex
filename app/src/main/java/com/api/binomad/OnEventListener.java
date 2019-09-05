package com.api.binomad;

public interface OnEventListener<T> {
    void onSuccess(T object);
    void onFailure(Exception e);
}