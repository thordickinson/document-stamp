package com.thord.docusafy.processor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
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
            state.setStrokeOpacity(options.getTransparency());
            over.setGState(state);

            xCenter = (pagesize.getLeft() + pagesize.getRight()) / 2;
            x = xCenter;

            y = instructionsFont.getSize() * 0.5f;
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER,
                    new Phrase("Valida la autenticidad de documento en docusafy.com", instructionsFont), xCenter,
                    y, 0);
            y += instructionsFont.getSize();

            float pageWidth = pagesize.getRight() - pagesize.getLeft();
            List<String> lines = splitText(info.getSubject(), pageWidth * 0.6f, subjectFont);
            Collections.reverse(lines);
            for (String line : lines) {
                y += subjectFont.getSize() * 0.3f;
                ColumnText.showTextAligned(over, Element.ALIGN_CENTER, new Phrase(line, subjectFont), x, y, 0);
                y += subjectFont.getSize();
            }

            y += docNameFont.getSize() * 0.3f;
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, new Phrase(info.getDocumentName(), docNameFont), x,
                    y, 0);
            y += docNameFont.getSize() * 1.5f;

            over.moveTo(pageWidth * 0.18f, 0);
            over.lineTo(pageWidth * 0.18f, y);
            over.stroke();

            over.moveTo(pageWidth * 0.82f, 0);
            over.lineTo(pageWidth * 0.82f, y);
            over.stroke();

            over.moveTo(pageWidth * 0.18f, y);
            over.lineTo(pageWidth * 0.82f, y);
            over.stroke();

            for (; y < pagesize.getTop(); y += yOffset) {
                for (x = 0; x < pagesize.getWidth() + xOffset; x += xOffset) {
                    float[] coords = rotate(x, y, options.getRotation());
                    ColumnText.showTextAligned(over, Element.ALIGN_LEFT, hashPhrase, coords[0],
                            coords[1], angle(options.getRotation()));
                }
            }

            over.restoreState();
        }
        stamper.close();
        reader.close();
    }

    private List<String> splitText(String text, float maxWidth, Font font) {
        List<String> words = Arrays.asList(text.split(" "));
        BaseFont baseFont = font.getCalculatedBaseFont(true);
        List<String> line = new ArrayList<>();
        List<List<String>> lines = new LinkedList<>();
        lines.add(line);

        for (String word : words) {
            String nextLine = String.join(" ", line) + " " + word;
            float width = baseFont.getWidthPoint(nextLine, font.getSize());
            if (width > maxWidth) {
                line = new ArrayList<>();
                lines.add(line);
            }
            line.add(word);
        }
        return lines.stream().map(l -> String.join(" ", l)).collect(Collectors.toList());
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
                "Documento compartido con la finalidad de hacer algo muy bonito pero no se supone que debería ser compartido para otras cosas",
                LocalDate.now()));
    }

}
