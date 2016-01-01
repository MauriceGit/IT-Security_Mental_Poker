
public class AnimatedCoinFlip {
    
    public static void main(String[] args) {
        
    	boolean isServer = false;
    	boolean useTLS = false;
    	
        if (args.length != 0 && args[0].matches("^START$")) {
            isServer = true;
        }
            	
        CoinFlipThread coinFlip = new CoinFlipThread(isServer, useTLS);
        AnimationThread animation = new AnimationThread();
        
        Thread coinFlipThread = new Thread(coinFlip);
        Thread animationThread = new Thread(animation);
        
        coinFlipThread.start();
        
        if (!isServer) {
        	animationThread.start();
        }
        
        try {
            coinFlipThread.join();
        } catch (InterruptedException e) {
            System.out.println("The main Thread got interrupted... maybe?");
        }
        
        System.out.println("finished ? " + coinFlip.isFinished());
        System.out.println("won      ? " + coinFlip.isWon());
        
        try {
            Thread.sleep(15000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Jetzt dinger setzen...");
        
        animation.setWinningState(coinFlip.isWon());
        animation.setFinishedState(coinFlip.isFinished());

    }

}
