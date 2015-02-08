package ru.po_znaika.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Random;

import ru.po_znaika.common.CommonException;

/**
 * Created by Rihter on 26.01.2015.
 * Provides tokens and credentials used for access to remote server
 */
public final class CacheAuthenticationProvider implements IAuthenticationProvider
{
    private static final String CacheFileName = "authentication_cache";

    private static final String LoginKey = "login_key";
    private static final String PasswordKey = "password_key";

    public CacheAuthenticationProvider(@NonNull Context _context)
    {
        m_cacheFile = _context.getSharedPreferences(CacheFileName, Context.MODE_PRIVATE);
    }

    public AuthenticationToken getAuthenticationToken() throws CommonException
    {
        throw new UnsupportedOperationException();
    }

    public void setAuthenticationToken() throws CommonException
    {
        throw new UnsupportedOperationException();
    }

    public LoginPasswordCredentials getLoginPasswordCredentials() throws CommonException
    {
        final String login = m_cacheFile.getString(LoginKey, null);
        if (login == null)
            return null;
        final String password = CryptoHelper.DeCrypt(m_cacheFile.getString(PasswordKey, null));

        return new LoginPasswordCredentials(login, password);
    }

    public void setLoginPasswordCredentials(@NonNull String login, @NonNull String password) throws CommonException
    {
        SharedPreferences.Editor cacheFileWriter = m_cacheFile.edit();
        cacheFileWriter.putString(LoginKey, login);
        cacheFileWriter.putString(PasswordKey, CryptoHelper.Crypt(password));
        cacheFileWriter.apply();
    }

    // TODO: use base64 + xor
    private static class CryptoHelper
    {
        private static final String CryptoAlphabet = "1234567890abcdefghijklmnopqrstuvwxyz";
        private static final int CryptoAppendixLength = 3;
        private static final int CryptoPostfixLength = 4;

        public static String getRandomString(int length)
        {
            Random rand = new Random(new Date().getTime());

            StringBuilder sb = new StringBuilder();
            for (int chIndex = 0; chIndex < length; ++chIndex)
                sb.append(CryptoAlphabet.charAt(rand.nextInt(CryptoAlphabet.length())));

            return sb.toString();
        }

        public static String Crypt(@NonNull String str)
        {
            return getRandomString(CryptoAppendixLength) + str + getRandomString(CryptoPostfixLength);
        }

        public static String DeCrypt(@NonNull String str)
        {
            return str.substring(CryptoAppendixLength, CryptoAppendixLength +
                    (str.length() - (CryptoAppendixLength + CryptoPostfixLength)));
        }
    }

    private SharedPreferences m_cacheFile;
}
