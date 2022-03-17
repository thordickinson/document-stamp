package com.thord.docusafy.processor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import com.itextpdf.text.BaseColor;
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
import static com.thord.docusafy.util.ITextUtil.*;
import com.thord.docusafy.util.HashUtil;

public class HashProcessor {
    
    private final String hashSalt;

    public HashProcessor(String salt){
        this.hashSalt = salt;
    }

    public void sign(String sourcePath, String targetPath, SignInfo info)
            throws IOException, DocumentException {
        sign(sourcePath, targetPath, info, new SignOptions());
    }

    private float[] rotate(float x, float y, float rotation) {
        float radian = (rotation * (float) Math.PI) / 180;
        float cos = (float) Math.cos(radian);
        float sin = (float) Math.sin(radian);
        float x1 = x * cos - y * sin;
        float y1 = y * cos + x * sin;
        return new float[] { x, y };
    }

    private float angle(float rotation) {
        return 90f - rotation;
    }

    public void sign(String sourcePath, String targetPath, SignInfo info, SignOptions options)
            throws IOException, DocumentException {
        BaseColor color = parseColor(options.getColor(), new GrayColor(0.3f));
        Font baseFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, color);

        Font safyCodeFont = new Font(baseFont.getBaseFont(), 10, Font.BOLD);

        Font docNameFont = deriveFont(baseFont, 2);
        Font subjectFont = deriveFont(baseFont, -1);
        Font instructionsFont = new Font(baseFont.getBaseFont(), 9);

        String hash = getHash(info);
        Phrase hashPhrase = new Phrase("SafyCode:" + hash.substring(0, 7), safyCodeFont);


        PdfReader reader = new PdfReader(sourcePath);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(targetPath));

        PdfContentByte over;
        Rectangle pagesize;
        float x, y, xCenter;
        float xOffset = getWidth(hashPhrase);
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
            state.setFillOpacity(options.getTransparency());

            over.setGState(state);

            for (y = 0; y < pagesize.getTop(); y += yOffset) {
                for (x = 0; x < pagesize.getWidth() + xOffset; x += xOffset) {
                    float[] coords = rotate(x, y, options.getRotation());
                    ColumnText.showTextAligned(over, Element.ALIGN_LEFT, hashPhrase, coords[0],
                            coords[1], angle(options.getRotation()));
                }
            }

            xCenter = (pagesize.getLeft() + pagesize.getRight()) / 2;
            x = xCenter;
            y = (pagesize.getTop() + pagesize.getBottom()) / 2;
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, new Phrase(info.getDocumentName(), docNameFont), x,
                    y, options.getRotation());
            y += docNameFont.getSize() * 1.5;
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, new Phrase(info.getSubject(), subjectFont), x,
                    y, options.getRotation());
            y += subjectFont.getSize() * 1.5;
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("Valida la autenticidad de documento en docusafy.com", instructionsFont), xCenter,
                    instructionsFont.getSize() * 0.25f, 0);
            
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
        return HashUtil.sha256(data);
    }

    public static void main(String... args) throws IOException, DocumentException{
        HashProcessor processor = new HashProcessor("749288de-ef62-4078-973c-55336f9890db");
        processor.sign("data/original.pdf", "data/hash-signed.pdf", new SignInfo("Cedula de ciudadanía",
                "Para algo importante", LocalDate.now()));
    }

}
