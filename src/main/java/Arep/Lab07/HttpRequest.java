package Arep.Lab07;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private String path;
    private String query;
    private Map<String, String> queryParams = new HashMap<>();

    public HttpRequest(String struripath) {
        if (struripath.contains("?")) {
            String[] pathParts = struripath.split("\\?", 2);
            this.path = pathParts[0];
            this.query = pathParts[1];
            parseQueryParams(this.query);
        } else {
            this.path = struripath;
            this.query = "";
        }
    }

    private void parseQueryParams(String query) {
        if (query == null || query.isEmpty()) return;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2) {
                queryParams.put(kv[0], kv[1]);
            } else if (kv.length == 1) {
                queryParams.put(kv[0], "");
            }
        }
    }

    public String getValues(String key) {
        return queryParams.getOrDefault(key, "");
    }

    public String getValue(String key) {
        return getValues(key);
    }

    public String getPath()  { return path; }
    public String getQuery() { return query; }
}