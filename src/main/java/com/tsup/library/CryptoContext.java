package com.tsup.library;

import javax.crypto.SecretKey;
import java.util.Map;

public class CryptoContext {
    private SecretKey aeadKey;

    public CryptoContext(SecretKey aeadKey) {
        this.aeadKey = aeadKey;
    }

    public synchronized SecretKey getAeadKey() {
        return aeadKey;
    }
}

