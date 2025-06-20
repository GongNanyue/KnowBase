package org.example.backend.model;

import java.util.List;

public class ChatResponse {
    private String answer;
    private List<String> references;
    private long timestamp;

    public ChatResponse(String answer, List<String> references) {
        this.answer = answer;
        this.references = references;
        this.timestamp = System.currentTimeMillis();
    }


    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<String> getReferences() { return references; }
    public void setReferences(List<String> references) { this.references = references; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}