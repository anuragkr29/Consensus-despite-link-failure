import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter the input file path : ");
        ReadFile fileReadObj = new ReadFile(s.nextLine());
        int numberOfProcesses = fileReadObj.getNumberOfProcesses();
        int numberOfRounds = fileReadObj.getNumberOfRounds();
        int messageDropNum = fileReadObj.getmessageDropNum();
        int[] inputVal = fileReadObj.getInputVal();
        System.out.println("--------- Input File read ----------");
        System.out.println(" Number of Processes : " + numberOfProcesses);
        System.out.println(" Number of rounds : " + numberOfRounds);
        System.out.println(" Message to drop : Every " + messageDropNum + "th message.");
        System.out.println(" Initial values : " + Arrays.toString(inputVal));
        System.out.println("------------------------------------");
        Round r = new Round(numberOfProcesses);
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfProcesses);
        Process processArr[] = new Process[numberOfProcesses];
        for (int i = 0; i < numberOfProcesses; i++) {
            Process p = new Process(i, numberOfProcesses, numberOfRounds, inputVal[i]);
            processArr[i] = p;
        }
        Communication channel = new Communication(processArr, messageDropNum, numberOfProcesses);
        for (int i = 0; i < numberOfProcesses; i++) {
            threadPool.submit(processArr[i]);
        }

        int round = -1;
        System.out.println("Dropping every " + messageDropNum +  "th message");
        while (round < numberOfRounds-1) {
            try {
                if (Round.threadCount.get() == 0) {
                    round++;
                    Thread.currentThread().sleep(1000);
                    r.nextRound(numberOfProcesses, round);
                    System.out.println("Started round : " + (round+1));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("All rounds finishied . Closing Thread pool");
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            System.out.println("Thread pool closed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}




