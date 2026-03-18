package Arep.Lab07;

public class HttpResponse {

    private int statusCode = 200;
    private String contentType = "text/html";
    private String body = "";

    public void setStatusCode(int code)    { this.statusCode = code; }
    public void setContentType(String ct)  { this.contentType = ct; }
    public void setBody(String body)       { this.body = body; }

    public int    getStatusCode()   { return statusCode; }
    public String getContentType()  { return contentType; }
    public String getBody()         { return body; }

    public String buildHeaders() {
        return "HTTP/1.1 " + statusCode + " OK\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "\r\n";
    }
}