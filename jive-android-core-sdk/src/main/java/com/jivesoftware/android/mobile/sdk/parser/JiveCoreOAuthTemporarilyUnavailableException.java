package com.jivesoftware.android.mobile.sdk.parser;

import com.jivesoftware.android.mobile.sdk.entity.ErrorEntity;
import com.jivesoftware.android.mobile.sdk.entity.SimpleErrorEntity;
import org.apache.http.HttpResponse;

import javax.annotation.Nonnull;

public class JiveCoreOAuthTemporarilyUnavailableException extends JiveCoreOAuthException {
    public static final int ERROR_CODE = SimpleErrorEntity.OAuth2ErrorType.TEMPORARILY_UNAVAILABLE.ordinal();
    public JiveCoreOAuthTemporarilyUnavailableException(@Nonnull HttpResponse httpResponse, int statusCode, @Nonnull ErrorEntity errorEntity) {
        super(httpResponse, statusCode, errorEntity);
    }
}
