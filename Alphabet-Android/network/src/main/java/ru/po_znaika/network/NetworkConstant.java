package ru.po_znaika.network;

/**
 * Created by Rihter on 19.01.2015.
 * External constants for using inside and outside (for external server collaboration modules).
 */
public class NetworkConstant
{
    public static final String ServiceDomainName = "po-znaika.ru/accounts";
    public static final String ServiceUrl = "http://" + ServiceDomainName + '/';

    public static final String LoginHeader = "Login";
    public static final String PasswordHeader = "Password";
}
