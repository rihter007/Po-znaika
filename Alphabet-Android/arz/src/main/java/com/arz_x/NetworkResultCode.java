package com.arz_x;

/**
 * Created by Rihter on 18.01.2015.
 * Describes different error situations with network communication
 */
public enum NetworkResultCode
{
    Unknown(372074748),                // a crc32 of 'NetworkResultCode.Unknown'
    NoConnection(251784210),           // a crc32 of 'NetworkResultCode.NoConnection'
    AuthenticationError(179229665);    // a crc32 of 'NetworkResultCode.AuthenticationError'

    NetworkResultCode(int _value)
    {
        m_value = _value;
    }

    public int getValue()
    {
        return m_value;
    }

    private int m_value;
}
