package ru.po_znaika.network;

/**
 * Created by Rihter on 18.01.2015.
 * Represents the exception that can be thrown by network library
 */
public class NetworkException extends Exception
{
    public NetworkException(NetworkResultCode resultCode)
    {
        m_resultCode = resultCode;
    }

    public NetworkResultCode getResultCode()
    {
        return m_resultCode;
    }

    private NetworkResultCode m_resultCode;
}
