package tracker.model;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Task {
    protected  String taskName;
    protected  String taskDescription;
    protected  EnumStatus taskStatus;
    protected  int taskID;
    protected TaskType taskType;
    protected int duration; // продолжительность в минутах
    protected Date startTime;


    public Task(String taskName, String taskDescription, EnumStatus status, TaskType taskType,
                int duration, Date startTime) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = status;
        this.taskType = taskType;
        this.duration = duration;
        this.startTime = startTime;
    }

    public void setTaskID(int taskID) {   // новый сеттер — только для менеджера
        this.taskID = taskID;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public EnumStatus getTaskStatus() {
        return taskStatus;
    }

    public int getTaskID() {
        return taskID;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public void setTaskStatus(EnumStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        if (startTime == null) {
            return null;
        }
        return new Date(startTime.getTime() + duration * 60_000L);
    }

    @Override
    public String toString() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd/HH:mm");
        String start = (startTime == null) ? "не задано" : fmt.format(startTime);
        Date end = getEndTime();
        String endStr = (end == null) ? "не задано" : fmt.format(end);

        return String.format(
                "Задача {Название: %s; Описание: %s; Статус: %s; ID: %d; " +
                        "Дата начала: %s; Дата окончания: %s}",
                taskName, taskDescription, taskStatus, taskID, start, endStr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskID == task.taskID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskID);
    }
}