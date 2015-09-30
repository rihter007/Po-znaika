package com.arz_x;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rihter on 18.01.2015.
 * Represents common operation result code
 */
public enum CommonResultCode
{
    UnknownReason(0xD7BA83EC),                   // a crc32 of 'CommonResult.UnknownReason'
    AssertError(0x69E30136),                     // a crc32 of 'CommonResult.AssertError'
    AccessDenied(0xD506CF53),                    // a crc32 of 'CommonResult.AccessDenied'
    NotFound(0x67E2835E),                        // a crc32 of 'CommonResult.NotFound'
    InvalidArgument(0x713ECFD6),                 // a crc32 of 'CommonResult.InvalidArgument'
    InvalidExternalSource(0x9DA1E1CF),           // a crc32 of 'CommonResult.InvalidExternalSource'
    InvalidInternalState(0x266D38A0);            // a crc32 of 'CommonResult.InvalidInternalState'

    CommonResultCode(int value)
    {
        m_value = value;
    }

    public int getValue()
    {
        return m_value;
    }

    private static final Map<Integer, CommonResultCode> TypesMap = new HashMap<Integer, CommonResultCode>()
    {
        {
            for (CommonResultCode code : CommonResultCode.values())
                put(code.getValue(), code);
        }
    };

    public static CommonResultCode getTypeByValue(int value)
    {
        return TypesMap.get(value);
    }

    private int m_value;
}
