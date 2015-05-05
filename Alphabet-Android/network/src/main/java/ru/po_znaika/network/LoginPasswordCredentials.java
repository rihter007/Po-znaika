package ru.po_znaika.network;

import android.text.TextUtils;

/**
 * Created by Rihter on 25.01.2015.
 * Container of login/password
 */
public class LoginPasswordCredentials
{
    public LoginPasswordCredentials() { }
    public LoginPasswordCredentials(String _login, String _password)
    {
        login = _login;
        password = _password;
    }

    public String login;
    public String password;

    public boolean isValid()
    {
        return !TextUtils.isEmpty(login) && !TextUtils.isEmpty(password);
    }
}
