package advisor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Menu {
    Scanner scanner;
    ArrayList<String> categories;
    boolean authenticated;
    String redirectUri = "https://accounts.spotify.com/authorize?client_id=a19ee7dbfda443b2a8150c9101bfd645&redirect_uri=http://localhost:8080&response_type=code";
    public Menu() {
        this.scanner = new Scanner(System.in);
        authenticated = false;
        categories = new ArrayList<>();
        categories.add("Top Lists");
        categories.add("Pop");
        categories.add("Mood");
        categories.add("Latin");
    }

    public void displayMenu() {
        boolean exit = false;
        while (!exit){
            String input = scanner.nextLine();
            if ("auth".equals(input)) {
                auth();
            } else if (!authenticated) {
                System.out.println("Please, provide access for application.");
            }
            if ("featured".equals(input) && authenticated)
                featured();
            else if ("new".equals(input) && authenticated) {
                listNewAlbums();
            } else if ("categories".equals(input) && authenticated) {
                showCategories();
            } else if (input.startsWith("playlists") && authenticated) {
                String categoryName = input.split(" ")[1].strip();
                showPlaylists(categoryName);
            } else if ("exit".equals(input)) {
                exit = true;
                close();
            }

        }

    }

    private void auth() {

        authenticated = true;
        System.out.println(redirectUri);
        System.out.println("---SUCCESS---");
    }

    private void showPlaylists(String categoryName) {
        System.out.println("---%s PLAYLISTS---".formatted(categoryName.toUpperCase()));
        System.out.println("Walk Like A Badass  \n" +
                "Rage Beats  \n" +
                "Arab Mood Booster  \n" +
                "Sunday Stroll");
    }

    private void close() {
        System.out.println("---GOODBYE!---");
    }

    private void showCategories() {
        // a list of all available categories on Spotify (just their names)
        System.out.println("---CATEGORIES---\n");
        categories.forEach(System.out::println);
    }

    private void listNewAlbums() {
        //a list of new albums with artists and links on Spotify;
        System.out.println("---NEW RELEASES---\n");
        System.out.println("Mountains [Sia, Diplo, Labrinth]\n" +
                "Runaway [Lil Peep]\n" +
                "The Greatest Show [Panic! At The Disco]\n" +
                "All Out Life [Slipknot]");
    }

    private void featured() {
        //a list of Spotify-featured playlists with their links fetched from API;
        System.out.println("---FEATURED---");
        System.out.println("Mellow Morning\n" +
                "Wake Up and Smell the Coffee\n" +
                "Monday Motivation\n" +
                "Songs to Sing in the Shower");
    }
}
