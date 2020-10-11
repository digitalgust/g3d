package g3dtest.game;

public class LogicUpdater extends Thread {
    GamePanel gpanel;
    boolean exit=false;

    public LogicUpdater(GamePanel gamePanel) {
        this.gpanel = gamePanel;
    }

    public void run() {
        while (!exit) {
            try {

                if (gpanel.logicCount == gpanel.renderCount) {
                    gpanel.logicUpdate();
                    gpanel.logicCount++;
                    synchronized (gpanel) {
                        gpanel.notifyAll();
                    }
                }
                synchronized (gpanel) {
                    gpanel.wait(1000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}