package ru.po_znaika.network;

import android.support.annotation.NonNull;

import com.arz_x.CommonException;
import com.arz_x.NetworkException;

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
     * @throws com.arz_x.CommonException
     * @throws NetworkException
     */
    AuthenticationToken authenticate(@NonNull String login, String password)
            throws CommonException, NetworkException;
}
