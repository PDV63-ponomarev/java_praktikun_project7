package tracker.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileTask {

    public static String toString(Task task) {
        String epicId = "";
        if (task instanceof TaskSubtask sub) {
            epicId = String.valueOf(sub.getEpicId());
        }

        String startTimeStr = "";
        if (task.getStartTime() != null) {
            startTimeStr = String.valueOf(task.getStartTime().getTime()); // миллисекунды
        }

        return String.format("%d,%s,%s,%s,%s,%s,%d,%s",
                task.getTaskID(),
                task.getTaskType(),
                task.getTaskName(),
                task.getTaskStatus(),
                task.getTaskDescription(),
                epicId,
                task.getDuration(),
                startTimeStr);
    }

    public static Task fromString(String value) {
        String[] f = value.split(",", -1);
        int id = Integer.parseInt(f[0].trim());
        TaskType type = TaskType.valueOf(f[1].trim());
        String name = f[2].trim();
        EnumStatus status = EnumStatus.valueOf(f[3].trim());
        String description = f[4].trim();
        String epicField = f[5].trim();
        int duration = Integer.parseInt(f[6].trim());

        Date startTime = null;
        String startTimeStr = f[7].trim();
        if (!startTimeStr.isEmpty()) {
            startTime = new Date(Long.parseLong(startTimeStr));
        }

        Task task = switch (type) {
            case TASK    -> new Task(name, description, status, type, duration, startTime);
            case EPIC    -> new TaskEpic(name, description, status, type, duration, startTime);
            case SUBTASK -> new TaskSubtask(name, description, status,
                    Integer.parseInt(epicField), type, duration, startTime);
        };
        task.setTaskID(id);   // id восстанавливаем из файла
        return task;
    }

    public static String historyToString(List<Task> history) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(history.get(i).getTaskID());
        }
        return sb.toString();
    }

    public static List<Integer> historyFromString(String value) {
        List<Integer> ids = new ArrayList<>();
        if (value == null || value.isBlank()) return ids;
        for (String s : value.split(",")) {
            ids.add(Integer.parseInt(s.trim()));
        }
        return ids;
    }
}