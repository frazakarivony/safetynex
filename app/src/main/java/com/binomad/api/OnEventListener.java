package com.binomad.api;

public interface OnEventListener<T> {
    void onSuccess(T object);
    void onFailure(Exception e);
}