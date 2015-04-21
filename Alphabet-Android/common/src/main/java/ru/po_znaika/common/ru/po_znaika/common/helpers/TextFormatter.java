package ru.po_znaika.common.ru.po_znaika.common.helpers;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;

/**
 * Created by Rihter on 08.04.2015.
 * Processes custom formating of the text
 */
public class TextFormatter
{
    private interface IFormatterProcessor
    {
        void Process(@NonNull List<String> values, @NonNull TextFormatBlock outTextFormatBlock) throws CommonException;
    }

    private static final class ColorFormatter implements IFormatterProcessor
    {
        public void Process(@NonNull List<String> values, @NonNull TextFormatBlock outTextFormatBlock) throws CommonException
        {
            if (values.size() == 0)
                return;
            if (values.size() > 2)
                throw new CommonException(CommonResultCode.InvalidArgument);

            outTextFormatBlock.setARGBColor((int)Long.parseLong(values.get(0), 16));
        }
    }

    private static final String FormatTag = "format";
    private static final char OpenTagBracket = '<';
    private static final char CloseTagBracket = '>';
    private static final String OpenFormatTag = OpenTagBracket + FormatTag;
    private static final String CloseFormatTag = OpenTagBracket + "/" + FormatTag + CloseTagBracket;

    private static final Map<String, IFormatterProcessor> FormatterCollection = new HashMap<String, IFormatterProcessor>()
    {
        {
            put("color", new ColorFormatter());
        }
    };

    public static List<TextFormatBlock> processText(@NonNull String text) throws CommonException
    {
        List<TextFormatBlock> formattedText = new ArrayList<>();

        int startPos = 0;
        while (startPos < text.length())
        {
            final int openTagPos = text.indexOf(OpenFormatTag, startPos);
            if (openTagPos == -1)
                break;
            final int closeBracketPos = text.indexOf(CloseTagBracket, openTagPos);
            if (closeBracketPos == -1)
                throw new CommonException(CommonResultCode.InvalidArgument);
            final int closeTagPos = text.indexOf(CloseFormatTag, closeBracketPos + 1);
            if (closeTagPos == -1)
                throw new CommonException(CommonResultCode.InvalidArgument);

            // place block between startPos and openTagPos:
            if (startPos < openTagPos)
                formattedText.add(new TextFormatBlock(text.substring(startPos, openTagPos)));

            // process found formatted block:
            TextFormatBlock formatBlock = new TextFormatBlock(text.substring(closeBracketPos + 1, closeTagPos));

            final Map<String, List<String>> allFormatValues = getAllValuePairs(
                    text.substring(openTagPos + OpenFormatTag.length(), closeBracketPos));

            final Set<Map.Entry<String, IFormatterProcessor>> availibleFormatters = FormatterCollection.entrySet();
            for (Map.Entry<String, IFormatterProcessor> formatProcessor :availibleFormatters )
            {
                List<String> formatValues = allFormatValues.get(formatProcessor.getKey());
                if (formatValues != null)
                    formatProcessor.getValue().Process(formatValues, formatBlock);
            }
            formattedText.add(formatBlock);

            startPos = closeTagPos + CloseFormatTag.length();
        }

        // put last text fragment 'as is'
        TextFormatBlock lastFormatBlock = new TextFormatBlock(text.substring(startPos));
        formattedText.add(lastFormatBlock);

        return formattedText;
    }

    private static Map<String, List<String>> getAllValuePairs(@NonNull String text)
    {
        Map<String, List<String>> result = new HashMap<>();

        final String[] valuePairs = text.split("\\s");
        for (String valuePair : valuePairs)
        {
            final int delimiterIndex = valuePair.indexOf('=');
            if (delimiterIndex == -1)
                continue;

            final String formatterName = valuePair.substring(0, delimiterIndex);

            String internalValue = getInternalValue(valuePair, delimiterIndex + 1, '"');
            if (internalValue == null)
            {
                internalValue = getInternalValue(valuePair, delimiterIndex + 1, '\'');
                if (internalValue == null)
                    continue;
            }

            List<String> values = result.get(formatterName);
            if (values == null)
            {
                values = new ArrayList<>();
                result.put(formatterName, values);
            }
            values.add(internalValue);
        }

        return result;
    }

    private static String getInternalValue(@NonNull String text, int startPos, char outsideChar)
    {
        final int openCharPos = text.indexOf(outsideChar, startPos);
        if (openCharPos == -1)
            return null;
        final int closeCharPos = text.indexOf(outsideChar, openCharPos + 1);
        if (closeCharPos == -1)
            return null;

        return text.substring(openCharPos + 1, closeCharPos);
    }
}
