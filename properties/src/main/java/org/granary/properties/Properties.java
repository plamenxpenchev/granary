package org.granary.properties;

import org.granary.properties.exception.PropertyException;
import org.granary.properties.exception.PropertyNotFoundException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class Properties {

    // No writers, so unnecessary to synchronize via a read-write lock or similar.
    private static final String PRS_NAME = "granary.prs";
    private static final int VALUE_CHAR_LENGTH_INCREMENT = 256;

    public static boolean getBoolean(PropertyKey property) throws PropertyException {
        char[] value = getSensitiveCharArray(property);
        String valueString = String.valueOf(value);
        wipeArray(value);
        return Boolean.parseBoolean(valueString);
    }

    public static long getLong(PropertyKey property) throws PropertyException {
        char[] value = getSensitiveCharArray(property);
        String valueString = String.valueOf(value);
        wipeArray(value);
        try {
            return Long.parseLong(valueString);
        } catch (NumberFormatException e) {
            throw new PropertyException(e);
        }
    }

    public static int getInt(PropertyKey property) throws PropertyException {
        char[] value = getSensitiveCharArray(property);
        String valueString = String.valueOf(value);
        wipeArray(value);
        try {
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            throw new PropertyException(e);
        }
    }

    public static String getString(PropertyKey property) throws PropertyException {
        char[] value = getSensitiveCharArray(property);
        String valueString = String.valueOf(value);
        wipeArray(value);
        return valueString;
    }

    public static char[] getSensitiveCharArray(PropertyKey property) throws PropertyException {
        if (property == null || property.toString() == null || property.toString().isEmpty()) {
            throw new PropertyException("Property key cannot be empty.");
        }
        String key = property.toString();
        URL prsResource = Properties.class.getClassLoader().getResource(PRS_NAME);
        if (prsResource == null) {
            throw new PropertyException(String.format("File '%s' not found.", PRS_NAME));
        }
        try (BufferedReader br = new BufferedReader(new FileReader(prsResource.getFile()))) {
            int next;
//            char nextChar;
            String keyRead = "";
            boolean readingKey = true;

            do {
                next = br.read();
                if (next < 0) {
                    break;
                }
//                nextChar = (char) next;
                switch (next) {
                    case '\n':
                        readingKey = true;
                        keyRead = "";
                        break;
                    case '=':
                        readingKey = false;
                        if (key.equals(keyRead)) {
                            return getSensitiveCharArrayFromLine(br, key);
                        }
                        break;
                    default:
                        if (readingKey) {
                            keyRead += (char) next;
                        }
                        break;
                }
            } while (next >= 0);

            throw new PropertyNotFoundException(key);
        } catch (IOException e) {
            throw new PropertyException(e);
        }
    }

    public static void wipeArray(char[] array) {
        Arrays.fill(array, '0'); // clear heap values
    }

    private static char[] getSensitiveCharArrayFromLine(BufferedReader br, String key) throws PropertyException {
        char[] sensitiveArr = new char[VALUE_CHAR_LENGTH_INCREMENT];
        int charactersProcessed = 0;
        try {
            int next;
            do {
                next = br.read();
                if (next < 0) {
                    break;
                }
                if (next == '\n') {
                    if (charactersProcessed == 0) {
                        throw new PropertyException(String.format("Property value for key '%s' is empty.", key));
                    }
                    break;
                } else if (next == '\r') {
                    continue;
                }

                if (charactersProcessed >= VALUE_CHAR_LENGTH_INCREMENT) {
                    char[] sensitiveArrReplace = new char[sensitiveArr.length + VALUE_CHAR_LENGTH_INCREMENT];
                    for (int i = 0; i < sensitiveArr.length; i++) {
                        sensitiveArrReplace[i] = sensitiveArr[i];
                    }
                    wipeArray(sensitiveArr);
                    sensitiveArr = sensitiveArrReplace;
                }

                sensitiveArr[charactersProcessed] = (char) next;
                charactersProcessed++;
            } while (next >= 0);

        } catch (IOException e) {
            throw new PropertyException(e);
        }
        char[] returnArr = new char[charactersProcessed];
        for (int i = 0; i < charactersProcessed; i++) {
            returnArr[i] = sensitiveArr[i];
        }
        wipeArray(sensitiveArr);
        return returnArr;
    }
}
