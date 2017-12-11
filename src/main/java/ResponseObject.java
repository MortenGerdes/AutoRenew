import java.util.HashMap;

public class ResponseObject {

    private int status;
    private String body;
    private HashMap<String, String> headerData = new HashMap<>();

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void addHeader(String header, String data){
        headerData.put(header, data);
    }

    public String getHeader(String header){
        return headerData.get(header);
    }
}