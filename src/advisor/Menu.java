package advisor;

import java.io.IOException;
import java.util.*;

public class Menu {
    Scanner scanner;
    Map<String, String> categoriesMap;
    boolean authenticated;
    static SimpleServer simpleServer;
    static StaticViewer viewer;
    public Menu(String accessUrl, String resourceUrl, int pageLimit) throws IOException {
        this.scanner = new Scanner(System.in);
        authenticated = false;
        simpleServer = new SimpleServer(accessUrl, resourceUrl);
        viewer = new StaticViewer(pageLimit);
        categoriesMap = new HashMap<>();

    }

    public void displayMenu() throws InterruptedException {
        boolean exit = false;
        while (!exit){
            String input = scanner.nextLine();
            if ("auth".equals(input)) {
                auth();
            } if ("exit".equals(input)) {
                exit = true;
                close();
            } else if (!authenticated) {
                System.out.println("Please, provide access for application.");
            }
             else if ("featured".equals(input) && authenticated)
                featured();
            else if ("new".equals(input) && authenticated) {
                listNewAlbums();
            } else if ("categories".equals(input) && authenticated) {
                showCategories();
            } else if (input.startsWith("playlists") && authenticated) {
                String categoryName = input.replaceFirst("playlists ", "");
                showPlaylists(categoryName);
            }

        }

    }

    private void auth() throws InterruptedException {
        simpleServer.getAccessToken();
        authenticated = true;
        categoriesMap = simpleServer.categoriesMap();
        System.out.println("---SUCCESS---");
    }

    private void showPlaylists(String categoryName) {
        System.out.println("%s".formatted(categoryName.toUpperCase()));
        if (!categoriesMap.containsKey(categoryName)){
            System.out.println("Unknown category name.");
        }else {
            System.out.println("---%s PLAYLISTS---".formatted(categoryName.toUpperCase()));
            String categoryID = categoriesMap.get(categoryName);
            List<String> playlists = simpleServer.playlistsInCategory(categoryID);
            viewer.showData(playlists);
        }

    }

    private void close() {
        simpleServer.close();
        System.out.println("---GOODBYE!---");
    }

    private void showCategories() {
        // a list of all available categories on Spotify (just their names)
        System.out.println("---CATEGORIES---\n");
        List<String> categoriesList = simpleServer.categoriesList();
        viewer.showData(categoriesList);
    }

    private void listNewAlbums() {
        //a list of new albums with artists and links on Spotify;
        System.out.println("---NEW RELEASES---\n");
        List<String> newAlbums = simpleServer.newAlbumsList();
        viewer.showData(newAlbums);

    }

    private void featured() {
        //a list of Spotify-featured playlists with their links fetched from API;
        System.out.println("---FEATURED---");
        List<String> featured = simpleServer.featuredList();
        viewer.showData(featured);
    }
}
