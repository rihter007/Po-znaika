package ru.po_znaika.common;

/**
 * Created by Rihter on 18.01.2015.
 */
public class CommonException extends Exception
{
    public CommonException(CommonResultCode value)
    {
        m_value = value;
    }

    public CommonResultCode getResultCode()
    {
        return m_value;
    }

    private CommonResultCode m_value;
}
