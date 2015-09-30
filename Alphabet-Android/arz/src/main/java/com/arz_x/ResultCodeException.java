package com.arz_x;

/**
 * Created by Rihter on 26.04.2015.
 * Base class for all result code exceptions
 */
public class ResultCodeException extends Exception
{
    public ResultCodeException(int resultCode)
    {
        this(resultCode, null);
    }

    public ResultCodeException(int resultCode, String message)
    {
        super(message);
        m_resultCode = resultCode;
    }

    public int getRawResultCode()
    {
        return m_resultCode;
    }

    @Override
    public String getMessage()
    {
        final String textMessage = super.getMessage();
        if (textMessage == null)
            return "error code: " + m_resultCode;
        return String.format("Error code: \"%d\", message: \"%s\"", m_resultCode, textMessage);
    }

    @Override
    public String toString()
    {
        return getClass().getName() + "'" + getMessage() + "'";
    }

    private int m_resultCode;
}
