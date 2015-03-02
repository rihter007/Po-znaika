package ru.po_znaika.network;

/**
 * Created by Rihter on 18.01.2015.
 * Describes different error situations with network communication
 */
public enum NetworkResultCode
{
    Unknown(0),
    NoConnection(1),
    AuthenticationError(1);

    private NetworkResultCode(int value)
    {
        m_value = value;
    }

    private int m_value;
}
