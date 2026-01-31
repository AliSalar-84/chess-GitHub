import javax.swing.*;

public class Game implements Runnable {

    private static void uncaughtException(Thread t, Throwable e) {
        System.err.println("❌ Uncaught exception in thread: " + t.getName());
        e.printStackTrace();
    }

    public void run() {
        SwingUtilities.invokeLater(new StartMenu());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Game());
        Thread.setDefaultUncaughtExceptionHandler(Game::uncaughtException);

    }

}
