package ru.po_znaika.alphabet;

import java.util.Random;

/**
 * Common helpers
 */
public class Helpers
{
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
}
