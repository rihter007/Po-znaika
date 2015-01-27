package ru.po_znaika.network;

import android.support.annotation.NonNull;

import ru.po_znaika.common.CommonException;

/**
 * Created by Rihter on 19.01.2015.
 * Needed for processing authentication
 */
public interface IAuthentication
{
    /**
     * Provides client authentication mechanism on server
     * @param login User logic to authenticate
     * @param password User password
     * @return Authentication token used if operation succeeded
     * @throws ru.po_znaika.common.CommonException
     * @throws NetworkException
     */
    AuthenticationToken authenticate(@NonNull String login, String password)
            throws CommonException, NetworkException;
}
