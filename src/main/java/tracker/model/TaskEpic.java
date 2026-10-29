package tracker.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class TaskEpic extends Task{
    private List<Integer> subtaskIds;
    private Date endTime;

    public TaskEpic(String taskName, String taskDescription, EnumStatus status, TaskType taskType,
                    int duration, Date startTime) {
        super(taskName, taskDescription, status, taskType, duration, startTime);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public void recalculate(List<TaskSubtask> subtasks) {

        List<TaskSubtask> mySubtasks = subtasks.stream()
                .filter(s -> subtaskIds.contains(s.getTaskID()))
                .toList();

        if (mySubtasks.isEmpty()) {
            // подзадач нет — ручные значения остаются, endTime считаем из них
            this.endTime = super.getEndTime();  // startTime + duration
            this.taskStatus = EnumStatus.NEW;
            return;
        }

        // --- время ---
        this.startTime = mySubtasks.stream()
                .map(TaskSubtask::getStartTime)
                .filter(Objects::nonNull)
                .min(Date::compareTo)
                .orElse(null);

        this.endTime = mySubtasks.stream()
                .map(TaskSubtask::getEndTime)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);

        this.duration = mySubtasks.stream()
                .mapToInt(TaskSubtask::getDuration)
                .sum();

        boolean allNew = true;
        boolean allDone = true;

        for (int id : subtaskIds) {
            for (TaskSubtask subtask : subtasks) {
                if (subtask.getTaskID() == id) {
                    if (!subtask.getTaskStatus().equals(EnumStatus.NEW)) {
                        allNew = false;
                    }
                    if (!subtask.getTaskStatus().equals(EnumStatus.DONE)) {
                        allDone = false;
                    }
                    break;
                }
            }
        }

        if (allNew) {
            this.taskStatus = EnumStatus.NEW;
        } else if (allDone) {
            this.taskStatus = EnumStatus.DONE;
        } else {
            this.taskStatus = EnumStatus.IN_PROGRESS;
        }
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd/HH:mm");
        String start = (startTime == null) ? "не задано" : fmt.format(startTime);
        Date end = getEndTime();
        String endStr = (end == null) ? "не задано" : fmt.format(end);

        return String.format("Задача {Название: %s; Описание: %s; Статус: %s; ID: %d; Подзадачи: %s; " +
                        "Дата начала: %s; Дата окончания: %s}",
                taskName,
                taskDescription,
                taskStatus,
                taskID,
                subtaskIds,
                start,
                endStr
        );
        
        
    }
}