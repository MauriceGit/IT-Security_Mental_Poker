

public class AnimatedCoinFlip {
    
    public static void main(String[] args) {
        
    	boolean isServer = false;
    	boolean useTLS = true;
    	
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
        
        System.out.println("Coin-Flip has finished...");
        
        animation.setWinningState(coinFlip.isWon());
        animation.setWinningState(false);
        animation.setFinishedState(coinFlip.isFinished());

    }

}
