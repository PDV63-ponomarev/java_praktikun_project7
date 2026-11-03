package tracker.manager;
import tracker.server.KVTaskClient;

public class Managers {

    // Приватный конструктор, чтобы нельзя было создать экземпляр класса
    private Managers() {
        // Пустой приватный конструктор
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefault(String url) {   // KVServer-backed
        return new HttpTaskManager(new InMemoryHistoryManager(), url);
    }
}
