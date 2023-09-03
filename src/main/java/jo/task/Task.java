package jo.task;
public class Task {
    protected String description;
    protected boolean isDone;

    public Task(String description, boolean isDone) {
        this.description = description;
        this.isDone = isDone;
    }

    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    public String toString() {
        return String.format("[T][%s] %s", this.getStatusIcon(), this.description);
    }

    public void mark(boolean isDone) {
        this.isDone = isDone;
    }

    public boolean getIsDone() {
        return this.isDone;
    }

    public String toFile() {
        return String.format("T | %s | %s", this.isDone ? "1" : "0", this.description);
    }

    public String getDescription() {
        return this.description;
    }
}
