package ru.po_znaika.common;

/**
 * Created by Rihter on 18.01.2015.
 */
public enum  CommonResultCode
{
    UnknownReason(0);

    private CommonResultCode(int value)
    {
        m_value = value;
    }

    private int m_value;
}
