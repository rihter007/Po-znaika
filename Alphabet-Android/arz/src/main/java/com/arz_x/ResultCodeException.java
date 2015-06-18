package com.arz_x;

/**
 * Created by Rihter on 26.04.2015.
 * Base class for all result code exceptions
 */
public class ResultCodeException extends Exception
{
    public ResultCodeException(int resultCode)
    {
        m_resultCode = resultCode;
    }

    public int getRawResultCode()
    {
        return m_resultCode;
    }

    @Override
    public String getMessage()
    {
        return "error code: " + m_resultCode;
    }

    @Override
    public String toString()
    {
        return getClass().getName() + "'" + getMessage() + "'";
    }

    private int m_resultCode;
}
