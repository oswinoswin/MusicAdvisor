package advisor;

import java.util.List;
import java.util.Scanner;

import static java.lang.Math.min;

public class StaticViewer {
    static int pageLimit;
    static int currentPage;
    static int lastPage;
    public StaticViewer(int pageLimit) {
        this.pageLimit = pageLimit;
    }

    public void showData(List<String> data){
        currentPage = 1;
        lastPage = data.size() % pageLimit == 0 ? data.size() / pageLimit : data.size() / pageLimit + 1;
        System.out.println("Data length: %d pageLimit: %d pages: %d".formatted(data.size(), pageLimit, lastPage));

        Scanner scanner = new Scanner(System.in);
        Boolean exit = false;
        printCurrentPage(data);
        while (!exit){
            switch (scanner.next()){
                case "next": {
                    next(data);
                    break;
                }
                case "prev": {
                    prev(data);
                    break;
                }
                case "exit": {
                    exit = true;
                    break;
                }
            }
        }
    }


    public void next(List<String> data){
        if (currentPage == lastPage){
            System.out.println("No more pages.");
            return;
        }
        currentPage++;
        printCurrentPage(data);
    }

    public void prev(List<String> data){
        if (currentPage == 1){
            System.out.println("No more pages.");
            return;
        }
        currentPage--;
        printCurrentPage(data);
    }

    public void printCurrentPage(List<String> data){
        printPageRecords(currentPage, data);
        System.out.println("---PAGE %d OF %d---".formatted(currentPage, lastPage));
    }

    public void printPageRecords(int pageNum, List<String> data){
        //for now assume page num correct
        int start = (pageNum - 1)*pageLimit;
        int end = min(start + pageLimit, data.size());
        for (int i = start; i < end; i++){
            System.out.println(data.get(i));
        }

    }

}
