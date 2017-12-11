import freemarker.template.Configuration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static spark.Spark.*;

public class Main {

    private FreeMarkerEngine fme;
    private List<String> cookies;
    private HttpsURLConnection conn;
    private Connection dbConn;

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception
    {
        Main http = new Main();
        port(1338);
        http.connectToDB();
        http.registerGetRoutes();
        http.registerPostRoutes();
    }

    public Main() throws IOException, SQLException {
        fme = new FreeMarkerEngine();
    }

    public int renewSubscription(String username, String password) throws Exception {
        String url = "https://ungdomsboligaarhus.dk/user";

        // make sure cookies is turn on
        CookieHandler.setDefault(new CookieManager());

        // 1. Send a "GET" request, so that we can extract the form's data.
        String page = getPageContent(url, false).getBody();
        String postParams = getFormParams(page, username, password);

        // 2. Construct above post's content and then send a POST request for authentication
        sendPost(url, postParams);

        return getPageContent(url, true).getStatus();
    }

    private void sendPost(String url, String postParams) throws Exception {

        URL obj = new URL(url);
        conn = (HttpsURLConnection) obj.openConnection();

        // Acts like a browser
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", "ungdomsboligaarhus.dk");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "da-DK,da;q=0.9,en-US;q=0.8,en;q=0.7,af;q=0.6");
        for (String cookie : this.cookies) {
            conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
        }
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", "https://ungdomsboligaarhus.dk/user");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Send post request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + postParams);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }

    private ResponseObject getPageContent(String url, boolean shouldBeLoggedIn) throws Exception {

        URL obj = new URL(url);
        ResponseObject ro = new ResponseObject();
        conn = (HttpsURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);

        // act like a browser
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "da-DK,da;q=0.9,en-US;q=0.8,en;q=0.7,af;q=0.6");
        if (cookies != null) {
            for (String cookie : this.cookies) {
                conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
        }
        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);

        BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Get the response cookies
        setCookies(conn.getHeaderFields().get("Set-Cookie"));
        if(!response.toString().contains("CPR nummer") && shouldBeLoggedIn)
        {
            responseCode = 400;
        }
        System.out.println("Response code: " + responseCode);
        ro.setStatus(responseCode);
        ro.setBody(response.toString());
        return ro;
    }

    public String getFormParams(String html, String username, String password)
            throws UnsupportedEncodingException {

        System.out.println("Extracting form's data...");

        Document doc = Jsoup.parse(html);

        Element loginform = doc.getElementById("user-login");
        Elements inputElements = loginform.getElementsByTag("input");
        List<String> paramList = new ArrayList<String>();
        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("pass");

            if (key.equals("name"))
                value = username;
            else if (key.equals("pass"))
                value = password;
            else if(key.equals("form_build_id"))
                value = "form-97U39QWcgXbsJ5QzakHBzR5kU0qGKZ0KJbvxDW9tJOI";
            else if(key.equals("form_id"))
                value = "user_login";
            else if(key.equals("op"))
                value = "Log+ind";
            paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
        }

        // build parameters list
        StringBuilder result = new StringBuilder();
        for (String param : paramList) {
            if (result.length() == 0) {
                result.append(param);
            } else {
                result.append("&" + param);
            }
        }
        return result.toString();
    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    public void registerGetRoutes()
    {
        get("/autorenew/register", (req, res) ->
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("message", "Welcome to my first maven project");
            return new ModelAndView(map, "register.ftl");
        }, fme);

        get("/autorenew", (req, res) ->
        {
            res.status(HttpServletResponse.SC_OK);
            return "<h> Your server works Morten!<h>";
        });
    }

    public void registerPostRoutes()
    {
        post("autorenew/register", (req, res) ->
        {
            int username, password, responseCode;
            String message = "Invalid username or password. Make sure your username and password <br>consists of purely numbers";
            Map<String, Object> map = new HashMap<>();
            Map<String, String> params = splitQuery(req.body());
            try {
                username = Integer.parseInt(params.get("username"));
                password = Integer.parseInt(params.get("password"));
            }
            catch(NumberFormatException e)
            {
                map.put("message", message);
                return new ModelAndView(map, "register.ftl");
            }
            responseCode = renewSubscription(username+"", password+"");
            if(responseCode == HttpServletResponse.SC_OK)
            {
                message = "You were logged in successfully on ungdomsboligaarhus, but weren't added to the database??? wut?.";
                if(insertIntoDB(username+"", password+""))
                {
                    message = "You were logged in on ungdomsboligaarhus and added to the database successfully. Enjoy your laziness";
                }
                map.put("message", message);
            }
            else
            {
                map.put("message", "Your credentials were rejected by ungdomsboligaarhus. Make sure you typed it right");
            }
            return new ModelAndView(map, "register.ftl");
        }, fme);
    }

    public static Map<String, String> splitQuery(String url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String query = url;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    private void connectToDB()
    {
        String url = "jdbc:mysql://localhost:3306/autorenew";
        String username = "root";
        String password = "morten@70";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            dbConn = DriverManager.getConnection(url, username, password);
            System.out.println("Driver loaded!");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Driver wasn't found!");
            e.printStackTrace();
        }
    }

    private boolean insertIntoDB(String username, String password)
    {
        int rowsAffected = 0;
        String query = "INSERT INTO autorenewdb (apply_number, pass) VALUES (?, ?)";
        try {
            PreparedStatement ps = dbConn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            rowsAffected = ps.executeUpdate();
            dbConn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (rowsAffected == 1);
    }

}
