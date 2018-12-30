package com.ictreport.ixi.exchange;

import com.google.gson.JsonObject;

public interface IPayload {

    JsonObject toJsonObject();
    String toJson();
    IPayload fromJson(String json);
    byte[] toJsonBytes();
    IPayload fromJsonBytes(byte[] bytes);
    String toJsonBytesBase64();
    IPayload fromJsonBytesBase64(String jsonBytesBase64);
}
