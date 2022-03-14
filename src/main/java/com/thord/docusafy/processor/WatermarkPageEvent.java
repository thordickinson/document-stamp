package com.thord.docusafy.processor;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.thord.docusafy.util.HashUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

public class WatermarkPageEvent {

    private static final String hashSalt = "749288de-ef62-4078-973c-55336f9890db";

    private static Font derive(Font font, int size){
        return new Font(font.getFamily(), size, font.getStyle(), font.getColor());
    }

    private static List<Phrase> getText(){
        // text watermark
        Font font = new Font(Font.FontFamily.HELVETICA, 34, Font.BOLD, BaseColor.RED);
        List<Phrase> text = new LinkedList<Phrase>();
        text.add(new Phrase("Copia de cédula de ciudadanía solo válida para", derive(font, 14)));
        text.add(new Phrase("Solicitud de arriendo local Villas de Granada", derive(font, 22)));
        text.add(new Phrase("No se autoriza ningún otro uso", derive(font, 20)));
        text.add(new Phrase("Fecha: Marzo 14 de 2022", derive(font, 18)));

        return text;
    }

    private void sign(String documentName, String subject, LocalDate date){
        String hash = getHash(documentName, subject, date);
    }

    private String normalize(String text){
        //TODO should we remove accents?
        return text.toLowerCase().trim().replaceAll(" +", " ");
    }

    private String getHash(String documentName, String subject, LocalDate date){
        String data = normalize(documentName) + ":" + normalize(subject) + ":" + date.toString() + hashSalt;
        return HashUtil.sha256(data).substring(0, 7);
    }

    public static void main(String... args) throws IOException, DocumentException {

        String fileName = "ampliada";
        // read existing pdf
        PdfReader reader = new PdfReader("data/" +fileName+  ".pdf");
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("data/" + fileName + "-watermarked.pdf"));
        List<Phrase> text = getText();

        // properties
        PdfContentByte over;
        Rectangle pagesize;
        float x, y;

        // loop over every page
        int n = reader.getNumberOfPages();
        for (int i = 1; i <= n; i++) {

            // get page size and position
            pagesize = reader.getPageSizeWithRotation(i);
            x = (pagesize.getLeft() + pagesize.getRight()) / 2;
            y = (pagesize.getTop() + pagesize.getBottom()) / 2;
            y = y + 150;
            over = stamper.getOverContent(i);
            over.saveState();

            // set transparency
            PdfGState state = new PdfGState();
            state.setFillOpacity(0.3f);
            over.setGState(state);

            for(Phrase phrase : text ){
                Font font = phrase.getFont();
                x += font.getSize() + (font.getSize() * 0.5);
                ColumnText.showTextAligned(over, Element.ALIGN_CENTER, phrase, x, y, 45f);
            }
            over.restoreState();
        }
        stamper.close();
        reader.close();
    }
}