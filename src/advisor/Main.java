package advisor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        String accessUrl = "https://accounts.spotify.com";
        String resourceUrl = "https://api.spotify.com";
        int pageLimit = 5;
        if(args.length >= 4) {
            accessUrl = args[1];
            resourceUrl = args[3];
        }
        if (args.length >= 6){
            pageLimit = Integer.valueOf(args[5]);
        }
        Menu menu = new Menu(accessUrl, resourceUrl, pageLimit);
        menu.displayMenu();
    }
}
