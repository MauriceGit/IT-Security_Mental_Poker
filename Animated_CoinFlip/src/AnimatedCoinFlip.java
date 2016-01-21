import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AnimatedCoinFlip {

    public static void main(String[] args) {

        Properties prop = new Properties();
        InputStream input = null;
        Boolean showAnimation = true;

        try {

            input = new FileInputStream("coinflip_config.conf");

            // load a properties file
            prop.load(input);

            showAnimation = Boolean.parseBoolean(prop
                    .getProperty("showAnimation"));

        } catch (IOException ex) {
            System.out
                    .println("There went something wrong when reading the config file:");
            System.out.println(ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }

        CoinFlipThread coinFlip = new CoinFlipThread();
        Thread coinFlipThread = new Thread(coinFlip);
        coinFlipThread.start();

        if (showAnimation) {

            AnimationThread animation = new AnimationThread();
            Thread animationThread = new Thread(animation);
            animationThread.start();

            try {
                coinFlipThread.join();
            } catch (InterruptedException e) {
                System.out.println("The main Thread got interrupted... maybe?");
            }

            animation.setWinningState(coinFlip.isWon());
            animation.setFinishedState(coinFlip.isFinished());
        } else {
            try {
                coinFlipThread.join();
            } catch (InterruptedException e) {
                System.out.println("The main Thread got interrupted... maybe?");
            }
        }

        System.out.println("finished ? " + coinFlip.isFinished());
        System.out.println("won      ? " + coinFlip.isWon());
    }

}
