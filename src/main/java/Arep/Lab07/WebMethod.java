package Arep.Lab07;
@FunctionalInterface
public interface WebMethod {
    String execute(HttpRequest req, HttpResponse res);
}