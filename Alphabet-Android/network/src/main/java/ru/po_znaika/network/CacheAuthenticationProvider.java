package ru.po_znaika.network;

import ru.po_znaika.common.CommonException;

/**
 * Created by Rihter on 26.01.2015.
 * Provides tokens and credentials used for access to remote server
 */
public class CacheAuthenticationProvider implements IAuthenticationProvider
{
    public AuthenticationToken getAuthenticationToken() throws NetworkException, CommonException
    {
        throw new UnsupportedOperationException();
    }

    public LoginPasswordCredentials getLogicPasswordCredentials() throws CommonException
    {
        throw new UnsupportedOperationException();
    }
}
