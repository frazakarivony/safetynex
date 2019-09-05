package com.api.safetynex.listener.interfaces;

public interface OnEventListener<T> {
    void onSuccess(T object);
    void onFailure(Exception e);
}