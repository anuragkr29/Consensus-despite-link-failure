import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class Process implements Runnable {

    private BlockingQueue<Message> queue;
    private int index;
    private int decision = -1;
    private int key;
    private int numberOfprocesses;
    private int numberOfRounds;
    private ArrayList<Integer> inputVal;
    private ArrayList<Integer> level;
    public boolean decisionDone = false;



    public Process(int index, int numberOfprocesses, int numberOfRounds, int initValue) {
        this.index = index;
        this.numberOfprocesses = numberOfprocesses;
        this.inputVal = new ArrayList<>(Collections.nCopies(numberOfprocesses, -1));
        this.inputVal.set(this.index, initValue);
        this.key = -1;
        this.numberOfRounds = numberOfRounds;
        this.queue =  new ArrayBlockingQueue<>(numberOfprocesses*10);
        this.level = new ArrayList<>(Collections.nCopies(numberOfprocesses, 0));
    }

    public void putMessage(Message m){
        queue.add(m);
    }

    public void processQueue() {
        try{
           if(!this.queue.isEmpty()) {
               while (!this.queue.isEmpty()) {
                   Message m = this.queue.take();
                   if(this.key==-1 && ((Integer) m.key).intValue()!=-1) {
                       this.key = ((Integer) m.key).intValue();
                   }
                   for (int i = 0; i < numberOfprocesses; i++) {
                       if (this.inputVal.get(i).intValue() == -1 && ((Integer) m.inputVal.get(i)).intValue()  != -1) {
                           this.inputVal.set(i,((Integer) m.inputVal.get(i)).intValue());
                       }
                       if (((Integer) this.level.get(i)).intValue() < ((Integer)m.level.get(i)).intValue()) {
                           this.level.set(i, (Integer)m.level.get(i));
                       }
                   }
               }
               this.level.set(this.index,Collections.min(this.level)+1);
               }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        }

        public void processDecision() {
            Optional<Integer> rowResult = this.inputVal.stream().reduce((a, b) -> (a & b));
            this.decision = (this.key != -1 && this.level.get(this.index) >= this.key && rowResult.get() == 1) ? 1 : 0;
            this.decisionDone = true;
        }

    @Override
    public void run() {
        Thread.currentThread().setName("Process : " + this.index);
        while (!this.decisionDone) {

            if (Round.round.get(this.index) == 100) {
                int globalRoundNum = Round.getGlobalRoundNumber();
                try {

                        if (globalRoundNum == 0 && this.index == 0) {
                            Random rand = new Random();
                            key = rand.nextInt(numberOfRounds-1) + 1;
                        } else if (globalRoundNum != 0) {
                            this.processQueue();
                        }


                        Message<Integer> toSend = new Message<>(this.key, this.inputVal, this.level);
                        Thread.sleep(1000);

                        Communication.sendMessage(toSend, this.index);

                        if(globalRoundNum == numberOfRounds-1) {
                            this.processDecision();
                        }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Round.update(this.index, 0);
            }
        }
        System.out.println("Shutting down " +  Thread.currentThread().getName() + " with decision : " + this.decision + " with level vector : " +
                "" + this.level.toString()+" with input vector " + this.inputVal.toString()+" with key "+this.key);
    }

}