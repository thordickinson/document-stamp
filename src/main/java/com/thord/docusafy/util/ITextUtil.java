package com.thord.docusafy.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;

public final class ITextUtil {
    /**
     * 
     * @param colorStr e.g. "#FF00FFAA" or "#FFFFFF"
     * @return
     */
    public static BaseColor parseColor(String hex, BaseColor defaultValue) {
        hex = hex.replace("#", "");
        switch (hex.length()) {
            case 6:
                return new BaseColor(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16));
            case 8:
                return new BaseColor(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16),
                        Integer.valueOf(hex.substring(6, 8), 16));
        }
        return defaultValue;
    }

    public static Font deriveFont(Font source, int delta) {
        return new Font(source.getBaseFont(), source.getSize() + delta);
    }

    public static float getWidth(Phrase phrase) {
        Font font = phrase.getFont();
        return font.getCalculatedBaseFont(true).getWidthPoint(phrase.getContent(), font.getSize());
    }
}
