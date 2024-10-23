package org.mini.g3d.core.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventDispatcher {
    List<Runnable> listeners = new CopyOnWriteArrayList();

    public void register(Runnable listener) {
        listeners.add(listener);
    }

    public void remove(Runnable listener) {
        listeners.remove(listener);
    }

    public void removeAll() {
        listeners.clear();
    }

    public void dispatch() {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                Runnable listener = listeners.get(i);
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
