package com.dmslabbert.taskmaster;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskMasterService {

    private final List<Task> tasks = new ArrayList<>();

    public void run() {
        while (true) {
            showList();
            var menuOption = getMenuOption();
            if ("Q".equals(menuOption)) {
                break;
            }
            if ("N".equals(menuOption)) {
                addItem();
            }
            if ("D".equals(menuOption)) {
                markAsDone();
            }
            if ("P".equals(menuOption)) {
                markAsPending();
            }
            if ("C".equals(menuOption)) {
                markAsCancelled();
            }
            if ("U".equals(menuOption)) {
                update();
            }
            if ("R".equals(menuOption)) {
                remove();
            }
        }
    }

    private void addItem() {
        var name = getInputString("New Task (Enter to quit)");
        if ("".equals(name)) return;

        var hasMatch = tasks.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name));
        if (name != null && !hasMatch) {
            var nextId = getNextId();
            tasks.add(new Task(nextId, name, Status.PENDING));
        }
    }

    private void markAsDone() {
        System.out.println("ID Status Description");
        List<Task> pendingTasks = getTasks(Status.PENDING);
        for (var todo : pendingTasks) {
            System.out.printf("%-2d %s %s\n", todo.getId(), symbolise(todo.getStatus()), todo.getName());
        }
        var taskId = getTaskId("Select Task ID to be Marked Done");
        if (taskId == 0) return;
        Optional<Task> taskOptional = pendingTasks.stream().filter(x -> x.getId() == taskId).findFirst();
        taskOptional.ifPresent(task -> task.setStatus(Status.DONE));
    }

    private void markAsPending() {
        System.out.println("ID Status Description");
        List<Task> candidateTasks = new ArrayList<>();
        candidateTasks.addAll(getTasks(Status.DONE));
        candidateTasks.addAll(getTasks(Status.CANCELLED));
        for (var todo : candidateTasks) {
            System.out.printf("%-2d %s %s\n", todo.getId(), symbolise(todo.getStatus()), todo.getName());
        }
        var taskId = getTaskId("Select Task ID to be Marked Pending");
        if (taskId == 0) return;
        Optional<Task> taskOptional = candidateTasks.stream().filter(x -> x.getId() == taskId).findFirst();
        taskOptional.ifPresent(task -> task.setStatus(Status.PENDING));
    }

    private void markAsCancelled() {
        List<Task> candidateTasks = new ArrayList<>();
        candidateTasks.addAll(getTasks(Status.PENDING));
        candidateTasks.addAll(getTasks(Status.DONE));
        showCandidateTasks(candidateTasks);
        var taskId = getTaskId("Select Task ID to be Marked Cancelled");
        if (taskId == 0) return;
        Optional<Task> taskOptional = candidateTasks.stream().filter(x -> x.getId() == taskId).findFirst();
        taskOptional.ifPresent(task -> task.setStatus(Status.CANCELLED));
    }

    private void update() {
        List<Task> candidateTasks = tasks.stream().sorted(Comparator.comparing(Task::getName)).toList();
        showCandidateTasks(candidateTasks);
        var taskId = getTaskId("Select Task ID to Update");
        if (taskId == 0) return;
        Optional<Task> taskOptional = candidateTasks.stream().filter(x -> x.getId() == taskId).findFirst();
        taskOptional.ifPresent(task -> {
            System.out.println("Old Description: " + task.getName());
            var name = getInputString("New Description (Enter to quit)");
            if ("".equals(name)) return;

            var hasMatch = tasks.stream().anyMatch(x -> x.getName().equals(name));
            if (name != null && !hasMatch) {
                task.setName(name);
            }
        });
    }

    private void remove() {
        List<Task> candidateTasks = tasks.stream().sorted(Comparator.comparing(Task::getName)).toList();;
        showCandidateTasks(candidateTasks);
        var taskId = getTaskId("Select Task ID to Remove");
        if (taskId == 0) return;
        Optional<Task> taskOptional = candidateTasks.stream().filter(x -> x.getId() == taskId).findFirst();
        taskOptional.ifPresent(tasks::remove);
    }

    private void showCandidateTasks(List<Task> candidateTasks) {
        System.out.println("ID Status Description");
        for (var todo : candidateTasks) {
            System.out.printf("%-2d %s %s\n", todo.getId(), symbolise(todo.getStatus()), todo.getName());
        }
    }

    private int getNextId() {
        Optional<Task> maxIdOptional = tasks.stream().max(Comparator.comparing(Task::getId));
        return maxIdOptional.map(task -> task.getId() + 1).orElse(1);
    }

    private String symbolise(Status status) {
        if (status == Status.DONE) {
            return "[D]";
        }
        if (status == Status.CANCELLED) {
            return "[C]";
        }
        return "[ ]";
    }

    private void showList() {
        System.out.println();
        System.out.println("*** TASK MASTER ***");
        System.out.println("ID Status Description");
        for (var todo : getTasks(Status.PENDING)) {
            System.out.printf("%2d %-6s %s\n", todo.getId(), symbolise(todo.getStatus()), todo.getName());
        }
        for (var todo : getTasks(Status.DONE)) {
            System.out.printf("%2d %-6s %s\n", todo.getId(), symbolise(todo.getStatus()), todo.getName());
        }
        for (var todo : getTasks(Status.CANCELLED)) {
            System.out.printf("%2d %-6s %s\n", todo.getId(), symbolise(todo.getStatus()), todo.getName());
        }
    }

    private List<Task> getTasks(Status cancelled) {
        return tasks.stream().
                filter(x -> x.getStatus().equals(cancelled))
                .sorted(Comparator.comparing(Task::getName))
                .toList();
    }

    private String getMenuOption() {
        while (true) {
            System.out.print("[N]ew, [D]one, [P]ending, [C]ancelled, [U]pdate, [R]emove, [Q]uit: ");
            Scanner in = new Scanner(System.in);
            List<String> options = Arrays.asList("N", "D", "P", "C", "U", "R", "Q");
            var option = in.nextLine().toUpperCase();
            if (options.contains(option)) {
                return option;
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
    }

    private String getInputString(String label) {
        System.out.print(label + ": ");
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    private int getTaskId(String label) {
        while (true) {
            try {
                System.out.print(label + " (0 to quit): ");
                Scanner in = new Scanner(System.in);
                return in.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid option. Try again.");
            }
        }
    }

}
