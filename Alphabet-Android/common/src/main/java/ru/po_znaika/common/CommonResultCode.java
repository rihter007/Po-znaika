package ru.po_znaika.common;

/**
 * Created by Rihter on 18.01.2015.
 * Represents common operation result code
 */
public enum  CommonResultCode
{
    UnknownReason(0),
    InvalidArgument(1),
    InvalidExternalSource(2),
    InvalidInternalState(3);

    private CommonResultCode(int value)
    {
        m_value = value;
    }

    public int getValue()
    {
        return m_value;
    }

    private int m_value;
}
