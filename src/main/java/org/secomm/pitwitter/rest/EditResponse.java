package org.secomm.pitwitter.rest;

public class EditResponse {

    private String result;

    private GlobalValues global;

    public EditResponse() {
    }

    public EditResponse(String result, GlobalValues global) {
        this.result = result;
        this.global = global;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public GlobalValues getGlobal() {
        return global;
    }

    public void setGlobal(GlobalValues global) {
        this.global = global;
    }

    @Override
    public String toString() {
        return "EditResponse{" +
                "result='" + result + '\'' +
                ", global=" + global +
                '}';
    }
}
