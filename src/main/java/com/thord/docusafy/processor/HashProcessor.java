package com.thord.docusafy.processor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.thord.docusafy.util.HashUtil;

public class HashProcessor {
    
    private final String hashSalt;

    public HashProcessor(String salt){
        this.hashSalt = salt;
    }



    public void sign(String sourcePath, SignInfo info, String targetPath) throws IOException, DocumentException{
        
        Font font = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new GrayColor(0.3f));
        String hash = getHash(info);
        Phrase hashPhrase = new Phrase(hash, font);


        PdfReader reader = new PdfReader(sourcePath);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(targetPath));
        PdfContentByte over;
        Rectangle pagesize;
        float x, y;
        float xOffset = 80;
        float yOffset = 80;

        // loop over every page
        int n = reader.getNumberOfPages();
        for (int i = 1; i <= n; i++) {

            // get page size and position
            pagesize = reader.getPageSizeWithRotation(i);
            over = stamper.getOverContent(i);
            over.saveState();

            // set transparency
            PdfGState state = new PdfGState();
            state.setFillOpacity(0.2f);
            over.setGState(state);

            for(y = 0; y < pagesize.getTop(); y += yOffset){
                for(x = y %2 == 0? 0 : (xOffset / 2); x < pagesize.getWidth(); x += xOffset){
                    ColumnText.showTextAligned(over, Element.ALIGN_LEFT, hashPhrase, x, y, 45f);
                }
            }

            x = (pagesize.getLeft() + pagesize.getRight()) / 2;
            y = (pagesize.getTop() + pagesize.getBottom()) / 2;
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, new Phrase(), x, y, 45f);
            
            over.restoreState();
        }
        stamper.close();
        reader.close();
    }


    private String normalize(String text){
        //TODO should we remove accents?
        return text.toLowerCase().trim().replaceAll(" +", " ");
    }

    public String getHash(SignInfo info){
        String data = normalize(info.getDocumentName()) + ":" + normalize(info.getSubject()) + ":" + info.getDate().toString() + hashSalt;
        return HashUtil.sha256(data).substring(0, 7);
    }

    public static void main(String... args) throws IOException, DocumentException{
        HashProcessor processor = new HashProcessor("749288de-ef62-4078-973c-55336f9890db");
        processor.sign("data/cedula.pdf", new SignInfo("Cedula de ciudadanía", 
        "Para algo importante", LocalDate.now()), "data/hash-signed.pdf");
    }

}
