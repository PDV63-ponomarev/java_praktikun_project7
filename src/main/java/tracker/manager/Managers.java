package tracker.manager;

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
}
