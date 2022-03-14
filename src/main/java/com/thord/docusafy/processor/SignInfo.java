package com.thord.docusafy.processor;

import java.time.LocalDate;

public class SignInfo {
    private final String documentName;
    private final String subject;
    private final LocalDate date;

    public SignInfo(String documentName, String subject, LocalDate date){
        this.documentName = documentName;
        this.subject = subject;
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDocumentName() {
        return documentName;
    }


    public String getSubject() {
        return subject;
    }

}
