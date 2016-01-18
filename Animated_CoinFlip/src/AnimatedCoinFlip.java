public class AnimatedCoinFlip {

    public static void main(String[] args) {

        boolean isServer = false;
        boolean useTLS = true;

        if (args.length != 0 && args[0].matches("^START$")) {
            isServer = true;
        }

        CoinFlipThread coinFlip = new CoinFlipThread(isServer, useTLS);
        Thread coinFlipThread = new Thread(coinFlip);
        coinFlipThread.start();

        if (!isServer) {
            
            AnimationThread animation = new AnimationThread();
            Thread animationThread = new Thread(animation);
            animationThread.start();

            try {
                coinFlipThread.join();
            } catch (InterruptedException e) {
                System.out.println("The main Thread got interrupted... maybe?");
            }

            System.out.println("finished ? " + coinFlip.isFinished());
            System.out.println("won      ? " + coinFlip.isWon());

            animation.setWinningState(coinFlip.isWon());
            animation.setFinishedState(coinFlip.isFinished());
        }
    }

}
