package tracker.manager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tracker.model.EnumStatus.NEW;
import static tracker.model.TaskType.TASK;


public class HistoryManagerTest{

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager(); // ← конкретная реализация
        task1 = new Task("T1", "d", NEW, TASK, 60, null);
        task2 = new Task("T2", "d", NEW, TASK, 60, null);
        task3 = new Task("T3", "d", NEW, TASK, 60, null);
        task1.setTaskID(1);   // id нужны, иначе дубли не отличить
        task2.setTaskID(2);
        task3.setTaskID(3);
    }

    @Test
    void historyShouldBeEmpty(){
        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void addShouldNotDuplicateAndMoveToEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);              // дубль

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "Дубль не должен увеличивать историю");
        assertEquals(task2, history.get(0), "Старый просмотр должен уйти вперёд");
        assertEquals(task1, history.get(1), "Повторный просмотр — в конце");
    }

    @Test
    void removeShouldDeleteFromHeadMiddleAndTail() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getTaskID());          // начало
        assertEquals(List.of(task2, task3), historyManager.getHistory());

        historyManager.remove(task3.getTaskID());          // конец
        assertEquals(List.of(task2), historyManager.getHistory());

        historyManager.add(task1);
        historyManager.add(task3);                          // снова 2,1,3... нет: 2,1,3
        historyManager.remove(task1.getTaskID());          // середина
        assertEquals(List.of(task2, task3), historyManager.getHistory());
    }

    @Test
    void removeNonExistingIdShouldDoNothing() {
        historyManager.add(task1);
        historyManager.remove(999);
        assertEquals(1, historyManager.getHistory().size());
    }
}

