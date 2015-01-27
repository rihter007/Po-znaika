package ru.po_znaika.network;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.Date;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;

import junit.framework.Assert;

/**
 * Created by Rihter on 19.01.2015.
 * Realization of authentication process
 */
public class Authenticator implements IAuthentication
{
    private static final String AuthenticationUrl = NetworkConstant.ServiceUrl + "/authenticate";

    public Authenticator()
    {
        try
        {
            m_url = new URL(AuthenticationUrl);
        }
        catch (Exception exp)
        {
            Assert.assertTrue("Invalid service url", false);
        }
    }

    @Override
    public AuthenticationToken authenticate(@NonNull String login, String password)
            throws CommonException, NetworkException
    {
        HttpURLConnection urlConnection = null;
        try
        {
            urlConnection = (HttpURLConnection) m_url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("login", login);
            urlConnection.setRequestProperty("password", (password == null) ? "" : password);

            final String tokenField = urlConnection.getHeaderField("token");
            final String expirationField = urlConnection.getHeaderField("expiration");
            if ((TextUtils.isEmpty(tokenField)) || (TextUtils.isEmpty(expirationField)))
                throw new NetworkException(NetworkResultCode.BrokenProtocol);
            long utcExpirationDate = 0;

            try
            {
                utcExpirationDate = Long.decode(expirationField);
            }
            catch (NumberFormatException exp)
            {
                throw new NetworkException(NetworkResultCode.BrokenProtocol);
            }

            AuthenticationToken token = new AuthenticationToken();
            token.authenticationToken  = tokenField;
            token.expirationDate = new Date(utcExpirationDate);
            return token;
        }
        catch (NetworkException exp)
        {
            throw exp;
        }
        catch (SocketException exp)
        {
            throw new NetworkException(NetworkResultCode.ConnectionTimeout);
        }
        catch (Exception exp)
        {
            Assert.assertTrue("Exception message: " + exp.getMessage(), false);
            throw new CommonException(CommonResultCode.UnknownReason);
        }
        finally
        {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    private URL m_url;
}
