package tracker.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskSubtask extends Task{
    private int epicId;

    public TaskSubtask(String taskName, String taskDescription, EnumStatus status,
                       int epicId, TaskType taskType, int duration, Date startTime) {
        super(taskName, taskDescription, status, taskType, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd/HH:mm");
        String start = (startTime == null) ? "не задано" : fmt.format(startTime);
        Date end = getEndTime();
        String endStr = (end == null) ? "не задано" : fmt.format(end);

        return String.format("Задача {Название: %s; Описание: %s; Статус: %s; ID: %d; Глобальная задача ID: %d; " +
                        "Дата начала: %s; Дата окончания: %s}",
                taskName,
                taskDescription,
                taskStatus,
                taskID,
                epicId,
                start,
                endStr
        );
    }
}