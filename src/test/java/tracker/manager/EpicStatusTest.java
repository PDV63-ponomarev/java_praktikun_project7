package tracker.manager;
import tracker.model.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tracker.model.EnumStatus.*;
import static tracker.model.TaskType.*;



public class EpicStatusTest {
    @Test
    void epicWithNoSubtasksShouldBeNew() {
        TaskEpic epic = new TaskEpic("E", "d", NEW, EPIC,
                60, null);

        epic.recalculate(List.of());

        assertEquals(NEW, epic.getTaskStatus(), "Статус эпика должен быть New");
    }

    @Test
    void epicWithAllNewSubtasksShouldBeNew() {
        TaskEpic epic = new TaskEpic("E", "d", NEW, EPIC,
                60, null);
        TaskSubtask subtask = new TaskSubtask("S", "d", NEW, epic.getTaskID(), SUBTASK,
                60, null);

        epic.addSubtaskId(subtask.getTaskID());
        epic.recalculate(List.of(subtask));

        assertEquals(NEW, epic.getTaskStatus(), "Статус эпика должен быть New");
    }

    @Test
    void epicWithAllDoneSubtasksShouldBeDone() {
        TaskEpic epic = new TaskEpic("E", "d", NEW, EPIC,
                60, null);
        TaskSubtask subtask1 = new TaskSubtask("S1", "d", DONE, epic.getTaskID(), SUBTASK,
                60, null);
        TaskSubtask subtask2 = new TaskSubtask("S2", "d", DONE, epic.getTaskID(), SUBTASK,
                60, null);

        subtask1.setTaskID(1);
        subtask2.setTaskID(2);

        epic.addSubtaskId(subtask1.getTaskID());
        epic.addSubtaskId(subtask2.getTaskID());

        epic.recalculate(List.of(subtask1, subtask2));

        assertEquals(DONE, epic.getTaskStatus(), "Статус эпика должен быть DONE");
    }

    @Test
    void epicWithNewAndDoneSubtasksShouldBeInProgress() {
        TaskEpic epic = new TaskEpic("E", "d", NEW, EPIC, 60, null);
        TaskSubtask subtask1 = new TaskSubtask("S1", "d", NEW, epic.getTaskID(), SUBTASK,
                60, null);
        TaskSubtask subtask2 = new TaskSubtask("S2", "d", DONE, epic.getTaskID(), SUBTASK,
                60, null);

        subtask1.setTaskID(1);
        subtask2.setTaskID(2);
        epic.addSubtaskId(subtask1.getTaskID());
        epic.addSubtaskId(subtask2.getTaskID());
        epic.recalculate(List.of(subtask1, subtask2));

        assertEquals(IN_PROGRESS, epic.getTaskStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void epicWithInProgressSubtaskShouldBeInProgress() {
        TaskEpic epic = new TaskEpic("E", "d", NEW, EPIC, 60, null);
        TaskSubtask subtask1 = new TaskSubtask("S1", "d", IN_PROGRESS, epic.getTaskID(), SUBTASK,
                60, null);
        TaskSubtask subtask2 = new TaskSubtask("S2", "d", IN_PROGRESS, epic.getTaskID(), SUBTASK,
                60, null);

        subtask1.setTaskID(1);
        subtask2.setTaskID(2);
        epic.addSubtaskId(subtask1.getTaskID());
        epic.addSubtaskId(subtask2.getTaskID());
        epic.recalculate(List.of(subtask1, subtask2));

        assertEquals(IN_PROGRESS, epic.getTaskStatus(), "Статус эпика должен быть IN_PROGRESS");
    }
}
