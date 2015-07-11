package ru.po_znaika.alphabet;

import android.support.annotation.NonNull;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * Common helpers
 */
public class Helpers
{
    public static int getMaxRowLength(@NonNull String[] rows)
    {
        int maxRowLength = 0;
        for (String str : rows)
        {
            if (maxRowLength < str.length())
                maxRowLength = str.length();
        }
        return maxRowLength;
    }

    public static <T> void randomSnuffle(T[] arr)
    {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());

        for (int itemIndex = 0; itemIndex < arr.length - 1; ++itemIndex)
        {
            final int ExchangeIndex = rand.nextInt(arr.length - itemIndex);

            if (itemIndex != ExchangeIndex)
            {
                final T destinationElement = arr[itemIndex];
                arr[itemIndex] = arr[ExchangeIndex];
                arr[ExchangeIndex] = destinationElement;
            }
        }
    }

    public static void randomSnuffle(char[] arr)
    {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());

        for (int itemIndex = 0; itemIndex < arr.length - 1; ++itemIndex)
        {
            final int ExchangeIndex = rand.nextInt(arr.length - itemIndex);

            if (itemIndex != ExchangeIndex)
            {
                final char destinationElement = arr[itemIndex];
                arr[itemIndex] = arr[ExchangeIndex];
                arr[ExchangeIndex] = destinationElement;
            }
        }
    }

    public static boolean isAscSequentialOrder(int[] arr)
    {
        if (arr.length == 0)
            return true;
        for (int i = 0; i < arr.length - 1; ++i)
        {
            if (arr[i] > arr[i + 1])
                return false;
        }
        return true;
    }
}
