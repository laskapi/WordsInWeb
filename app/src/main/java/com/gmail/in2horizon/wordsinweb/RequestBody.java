package com.gmail.in2horizon.wordsinweb;

public class RequestBody {
    public RequestBody(String q, String source, String target, String format, String api_key) {
        this.q = q;
        this.source = source;
        this.target = target;
        this.format = format;
        this.api_key = api_key;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    private String q,source,target,format,api_key;

}
