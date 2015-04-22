package com.arz_x;

/**
 * Created by Rihter on 18.01.2015.
 * Represents common operation result code
 */
public enum CommonResultCode
{
    UnknownReason(-675642388),                   // a crc32 of 'CommonResult.UnknownReason'
    CodeError(1009550380),                       // a crc32 of 'CommonResult.CodeError'
    InvalidArgument(1899941846),                 // a crc32 of 'CommonResult.InvalidArgument'
    InvalidExternalSource(-1650335281),          // a crc32 of 'CommonResult.InvalidExternalSource'
    InvalidInternalState(644692128);             // a crc32 of 'CommonResult.InvalidInternalState'

    CommonResultCode(int value)
    {
        m_value = value;
    }

    public int getValue()
    {
        return m_value;
    }

    private int m_value;
}
