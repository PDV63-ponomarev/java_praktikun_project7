package tracker.manager;

import tracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{

    // Класс узла двусвязного списка
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    // Голова и хвост двусвязного списка
    private Node head;
    private Node tail;

    // HashMap: id задачи -> узел списка
    private final HashMap<Integer, Node> nodeMap = new HashMap<>();

    // linkLast добавляет задачу в конец списка и возвращает созданный узел
    private Node linkLast(Task task) {
        Node newNode = new Node(task);
        if (tail == null) {          // список пуст
            head = newNode;
            tail = newNode;
        } else {                     // прицепляем к хвосту
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        return newNode;
    }

    // getTasks собирает все задачи из связного списка в ArrayList
    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks;
    }

    // removeNode вырезает узел из списка
    private void removeNode(Node node) {
        if (node == null) {
            return;
        }
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {                     // node — голова
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {                     // node — хвост
            tail = node.prev;
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        int id = task.getTaskID();

        // Если задача уже есть в истории — вырезаем старый узел
        Node oldNode = nodeMap.get(id);
        if (oldNode != null) {
            removeNode(oldNode);
        }

        // Добавляем в конец и запоминаем узел в мапе
        Node newNode = linkLast(task);
        nodeMap.put(id, newNode);

    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);   // забираем узел из мапы
        if (node != null) {
            removeNode(node);             // и вырезаем из списка
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}

