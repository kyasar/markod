package com.dopamin.markod.objects;

/**
 * Created by kadir on 19.10.2015.
 */
public interface TokenResult {
    public void tokenSuccess(String token);
    public void tokenExpired();
    public void tokenFailed();
}
