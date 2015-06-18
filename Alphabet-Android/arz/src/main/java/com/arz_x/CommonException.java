package com.arz_x;

/**
 * Created by Rihter on 18.01.2015.
 * Represents common operation exception
 */
public class CommonException extends ResultCodeException
{
    public CommonException(CommonResultCode value)
    {
        super(value.getValue());
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

    private CommonResultCode m_value;
}
