package ru.po_znaika.network;

/**
 * Created by Rihter on 18.01.2015.
 * Describes different error situations with network communication
 */
public enum NetworkResultCode
{
    ConnectionTimeout(0),
    BrokenProtocol(0),
    NotAuthenticated(1);

    private NetworkResultCode(int value)
    {
        m_value = value;
    }

    private int m_value;
}
