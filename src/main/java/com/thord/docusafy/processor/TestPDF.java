package com.thord.docusafy.processor;

import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class TestPDF {

    private static PDDocument getWatermark(PDDocument original) throws IOException {
        // Create a document and add a page to it
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        // Create a new font object selecting one of the PDF base fonts
        PDFont font = PDType1Font.HELVETICA_BOLD;

        // Start a new content stream which will "hold" the to be created content
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Define a text content stream using the selected font, moving the cursor and
        // drawing the text "Hello World"
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(100, 700);
        contentStream.showText("Hello World");
        contentStream.endText();


        // Make sure that the content stream is closed:
        contentStream.close();
        return document;
    }

    public static void main(String[] args) throws Exception {
        PDDocument realDoc = PDDocument.load(new File("./data/original.pdf"));
        // the above is the document you want to watermark
        // for all the pages, you can add overlay guide, indicating watermark the
        // original pages with the watermark document.

        PDDocument watermark = getWatermark(realDoc);
        HashMap<Integer, PDDocument> overlayGuide = new HashMap<Integer, PDDocument>();
        for (int i = 0; i < realDoc.getNumberOfPages(); i++) {
            overlayGuide.put(i + 1, watermark);
            // watermark.pdf is the document which is a one page PDF with your watermark
            // image in it.
            // Notice here, you can skip pages from being watermarked.
        }
        Overlay overlay = new Overlay();
        overlay.setInputPDF(realDoc);
        overlay.overlayDocuments(overlayGuide).save("./data/final.pdf");
        overlay.close();
    }
}