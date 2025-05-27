package com.example.doctorchecker.util;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.math.BigInteger;

public class StyleApplier {

    /**
     * Основной метод применения стилей ГОСТ к документу
     */
    public static void applyStyles(XWPFDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Документ не может быть null");
        }

        // 1. Применяем стиль ко всем параграфам
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            if (paragraph == null) continue;

            // Сбрасываем стиль параграфа на Normal
            paragraph.setStyle("Normal");

            // Применяем стиль ко всем частям текста в параграфе
            for (XWPFRun run : paragraph.getRuns()) {
                if (run == null) continue;

                // Установка шрифта
                run.setFontFamily("Times New Roman");

                // Размер шрифта (по умолчанию 12)
                if (run.getFontSize() <= 0 || run.getFontSize() > 14) {
                    run.setFontSize(12);
                }
            }

            // Абзацный отступ
            if (paragraph.getIndentationFirstLine() != 125) {
                paragraph.setIndentationFirstLine(125); // 1.25 см ≈ 125 twips
            }

            // Межстрочный интервал
            setLineSpacing(paragraph, 1.5);
        }

        // 2. Поля страницы
        setMargins(document);
    }

    // === Установка межстрочного интервала ===
    public static void setLineSpacing(XWPFParagraph paragraph, double lineSpacing) {
        CTSpacing spacing = getOrCreateSpacing(paragraph);

        int lineSpacingTwips = (int) (lineSpacing * 240); // 1.5 × 240 = 360

        spacing.setLine(BigInteger.valueOf(lineSpacingTwips));
        spacing.setLineRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule.EXACT);
    }

    private static CTSpacing getOrCreateSpacing(XWPFParagraph paragraph) {
        org.apache.xmlbeans.XmlObject[] spacingObj = paragraph.getCTP().selectPath(
                "declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:pPr/w:spacing");

        if (spacingObj.length > 0) {
            return (CTSpacing) spacingObj[0];
        } else {
            return paragraph.getCTP().addNewPPr().addNewSpacing();
        }
    }

    // === Поля страницы ===
    public static void setMargins(XWPFDocument document) {
        CTDocument1 ctDoc = document.getDocument();
        CTBody body = ctDoc.getBody();

        CTSectPr sectPr = body.isSetSectPr() ? body.getSectPr() : body.addNewSectPr();
        CTPageMar pageMar = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();

        // Поля в twips (1 мм ≈ 56.7 twips)
        pageMar.setLeft(BigInteger.valueOf(1067));   // Левое — 30 мм
        pageMar.setRight(BigInteger.valueOf(354));   // Правое — 10 мм
        pageMar.setTop(BigInteger.valueOf(1134));    // Верхнее — 20 мм
        pageMar.setBottom(BigInteger.valueOf(1701)); // Нижнее — 25 мм
    }
}