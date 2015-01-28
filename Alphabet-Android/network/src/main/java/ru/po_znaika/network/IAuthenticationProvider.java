package ru.po_znaika.network;

import android.support.annotation.NonNull;

import ru.po_znaika.common.CommonException;

/**
 * Created by Rihter on 19.01.2015.
 * Simple interface for similar way of setting authentication token
 */
public interface IAuthenticationProvider
{
    AuthenticationToken getAuthenticationToken() throws NetworkException, CommonException;
    LoginPasswordCredentials getLoginPasswordCredentials() throws CommonException;
}
