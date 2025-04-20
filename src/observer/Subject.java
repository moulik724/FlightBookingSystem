package observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject class that maintains observers and notifies them of events
 */
public class Subject {
    private List<Observer> observers = new ArrayList<>();
    
    public void registerObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    public void notifyObservers(String eventType, Object data) {
        for (Observer observer : observers) {
            observer.update(eventType, data);
        }
    }
}