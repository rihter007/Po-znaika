package ru.po_znaika.network;

import android.text.TextUtils;

/**
 * Created by Rihter on 25.01.2015.
 */
public class LoginPasswordCredentials
{
    public String login;
    public String password;

    public boolean isValid()
    {
        return !TextUtils.isEmpty(login) && TextUtils.isEmpty(password);
    }
}
