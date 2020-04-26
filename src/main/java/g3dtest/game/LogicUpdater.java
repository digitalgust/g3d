package g3dtest.game;

public class LogicUpdater extends Thread {
    GamePanel gpanel;

    public LogicUpdater(GamePanel gamePanel) {
        this.gpanel = gamePanel;
    }

    public void run() {
        while (true) {
            try {

                if (gpanel.logicCount == gpanel.renderCount) {
                    gpanel.logicUpdate();
                    gpanel.logicCount++;
                    synchronized (gpanel) {
                        gpanel.notifyAll();
                    }
                }
                synchronized (gpanel) {
                    gpanel.wait();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}