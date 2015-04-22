package com.arz_x;

/**
 * Created by Rihter on 18.01.2015.
 * Represents common operation exception
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

    @Override
    public String getMessage()
    {
        return "error code: " + m_value.name();
    }

    @Override
    public String toString()
    {
        return CommonException.class.getName() + ", " + getMessage();
    }

    private CommonResultCode m_value;
}
