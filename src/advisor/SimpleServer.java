package advisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class SimpleServer {
    private String code="";
    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String CLIENT_SECRET = "CLIENT_SECRET";
    private static final String REDIRECT_URI = "http://localhost:8080";
    private String accessUrl;
    private String featuredUrl;
    private String releasesUrl;
    private String playlistUrl;
    private String categoriesUrl;

    private String accessToken;

    private HttpClient httpClient;


    HttpServer server;
    public SimpleServer(String accessUrl, String resourceUrl) throws IOException {
        this.accessUrl = accessUrl;
        this.featuredUrl = resourceUrl + "/v1/browse/featured-playlists";
        this.releasesUrl = resourceUrl + "/v1/browse/new-releases";
        this.categoriesUrl = resourceUrl + "/v1/browse/categories";
        this.playlistUrl = resourceUrl + "/v1/browse/categories/%s/playlists"; //category_id needed
        this.httpClient = HttpClient.newHttpClient();
        startServer();
    }

    public void startServer() throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);

        server.createContext("/",
                new HttpHandler() {
                    public void handle(HttpExchange exchange) throws IOException {

                        System.out.println("Received: " + exchange.toString());
                        String message = "Authorization code not found. Try again.";
                        if (exchange.getRequestURI().getQuery() != null) {
                            String query = exchange.getRequestURI().getQuery();
                            if (query.startsWith("code=")) {
                                code = query.split("=")[1];
                                message = "Got the code. Return back to your program.";
                            }

                        }
                        exchange.sendResponseHeaders(200, message.length());
                        exchange.getResponseBody().write(message.getBytes());
                        exchange.getResponseBody().close();
                    }
                }
        );
        server.start();
    }
    public void close(){
        server.stop(1);
    }
    public void getAccessToken() throws InterruptedException {
        code = "";
        System.out.println("use this link to request the access code:");
        System.out.println("%s/authorize?client_id=%s&redirect_uri=%s&response_type=code".formatted(accessUrl, CLIENT_ID, REDIRECT_URI));
        System.out.println("waiting for code...");
        while (code.equals("")){
            Thread.sleep(1);
        }
        server.stop(1);
        System.out.println("code received");
        System.out.println("making http request for access_token...");
        //ok, we got access code now, lets send POST to accounts.spotify.com/api/token to get access token
        URI accesTokenUri = URI.create("%s/api/token".formatted(accessUrl));
        String tokenRequestBody = "grant_type=authorization_code&code=%s&redirect_uri=%s".formatted(code, REDIRECT_URI);
        System.out.println("Trying to send: " + tokenRequestBody);
        String authorizationToken = getBasicAuthenticationHeader(CLIENT_ID, CLIENT_SECRET);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(accesTokenUri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", authorizationToken)
                .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                .build();
        System.out.println(request.headers());
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response:");
            System.out.println(response.body());
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            accessToken = jsonObject.get("access_token").getAsString();
        } catch (Exception e) {
            System.out.println("We cannot send data. Please, try later.");
        }

    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    public List<String> featuredList(){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(featuredUrl))
                .GET()
                .build();
        List<String> result = new LinkedList<>();
        try {
            var response = httpClient.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject playlists = jsonObject.getAsJsonObject("playlists");
            for (JsonElement item: playlists.getAsJsonArray("items")){
                JsonObject itemObject = item.getAsJsonObject();
                JsonObject externalUrls = itemObject.getAsJsonObject("external_urls");
                result.add("%s\n%s".formatted(itemObject.get("name").getAsString(),externalUrls.get("spotify").getAsString() ));
            }
            return result;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public List<String> newAlbumsList(){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(releasesUrl))
                .GET()
                .build();
        List<String> result = new LinkedList<>();
        try {
            var response = httpClient.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject albums = jsonObject.getAsJsonObject("albums");
            for (var item: albums.getAsJsonArray("items")){
                JsonObject itemAsJsonObject = item.getAsJsonObject();
                List<String> artistsNames = new ArrayList<>();
                for (var artist: itemAsJsonObject.getAsJsonArray("artists")){
                    JsonObject artistAsJsonObject = artist.getAsJsonObject();
                    artistsNames.add(artistAsJsonObject.get("name").getAsString());
                }
                result.add(itemAsJsonObject.get("name").getAsString() + "\n" + artistsNames + "\n" +
                        itemAsJsonObject.getAsJsonObject("external_urls").get("spotify").getAsString() + "\n");
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> categoriesMap() {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(categoriesUrl))
                .GET()
                .build();
        Map<String, String> categoriesMap = new HashMap<>();
        try {
            var response = httpClient.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            for (var item: jsonObject.getAsJsonObject("categories").getAsJsonArray("items")){
                String name = item.getAsJsonObject().get("name").getAsString();
                String id = item.getAsJsonObject().get("id").getAsString();
                categoriesMap.put(name, id);
            }
            return categoriesMap;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> categoriesList() {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(categoriesUrl))
                .GET()
                .build();
        List<String> result = new LinkedList<>();
        try {
            var response = httpClient.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            for (var item: jsonObject.getAsJsonObject("categories").getAsJsonArray("items")){
                String name = item.getAsJsonObject().get("name").getAsString();
                result.add(name);
            }
            return result;


        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> playlistsInCategory(String categoryID){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(playlistUrl.formatted(categoryID)))
                .GET()
                .build();
        List<String> result = new LinkedList<>();
        try {
            var response = httpClient.send(httpRequest,  HttpResponse.BodyHandlers.ofString());
            if(response.body().isEmpty() || response.body().contains("error") || !response.body().contains("playlists")){
                System.out.println("FUCKUPIK");
                return null;
            }

            JsonObject playlists = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonObject("playlists");
            for (var item: playlists.getAsJsonArray("items")){
                result.add(item.getAsJsonObject().get("name").getAsString() + "\n" +
                        item.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString() + "\n");
            }
            return result;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
