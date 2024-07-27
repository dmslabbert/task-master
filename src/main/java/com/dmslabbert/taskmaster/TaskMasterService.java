package com.dmslabbert.taskmaster;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskMasterService {

    private final List<Task> tasks = new ArrayList<>();

    public void run() {
        tasks.add(new Task(1, "Apples", Status.PENDING));
        tasks.add(new Task(2, "Bananas", Status.DONE));
        tasks.add(new Task(3, "Carrots", Status.CANCELLED));
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

        var hasMatch = tasks.stream().anyMatch(x -> x.getName().equals(name));
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
        var taskId = getTaskId();
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
        var taskId = getTaskId();
        if (taskId == 0) return;
        Optional<Task> taskOptional = candidateTasks.stream().filter(x -> x.getId() == taskId).findFirst();
        taskOptional.ifPresent(task -> task.setStatus(Status.PENDING));
    }

    private void markAsCancelled() {
        System.out.println("ID Status Description");
        List<Task> candidateTasks = new ArrayList<>();
        candidateTasks.addAll(getTasks(Status.PENDING));
        candidateTasks.addAll(getTasks(Status.DONE));
        for (var todo : candidateTasks) {
            System.out.printf("%-2d %s %s\n", todo.getId(), symbolise(todo.getStatus()), todo.getName());
        }
        var taskId = getTaskId();
        if (taskId == 0) return;
        Optional<Task> taskOptional = candidateTasks.stream().filter(x -> x.getId() == taskId).findFirst();
        taskOptional.ifPresent(task -> task.setStatus(Status.CANCELLED));
    }

    private void update() {
        System.out.println("ID Status Description");
        List<Task> candidateTasks = tasks;
        for (var todo : candidateTasks) {
            System.out.printf("%-2d %s %s\n", todo.getId(), symbolise(todo.getStatus()), todo.getName());
        }
        var taskId = getTaskId();
        if (taskId == 0) return;
        Optional<Task> taskOptional = candidateTasks.stream().filter(x -> x.getId() == taskId).findFirst();
        taskOptional.ifPresent(task -> {
            var name = getInputString("New Description (Enter to quit)");
            if ("".equals(name)) return;

            var hasMatch = tasks.stream().anyMatch(x -> x.getName().equals(name));
            if (name != null && !hasMatch) {
                task.setName(name);
            }
        });
    }

    private void remove() {
        System.out.println("ID Status Description");
        List<Task> candidateTasks = tasks;
        for (var todo : candidateTasks) {
            System.out.printf("%-2d %s %s\n", todo.getId(), symbolise(todo.getStatus()), todo.getName());
        }
        var taskId = getTaskId();
        if (taskId == 0) return;
        Optional<Task> taskOptional = candidateTasks.stream().filter(x -> x.getId() == taskId).findFirst();
        taskOptional.ifPresent(tasks::remove);
    }

    private int getNextId() {
        Optional<Task> maxIdOptional = tasks.stream().max(Comparator.comparing(Task::getId));
        return maxIdOptional.map(task -> task.getId() + 1).orElse(1);
    }

    private String symbolise(Status status) {
        if (status == Status.DONE) {
            return "[" + Character.toString(0x2713) + "]";
        }
        if (status == Status.CANCELLED) {
            return "[x]";
        }
        return "[ ]";
    }

    private void showList() {
        System.out.println("TASK LIST:");
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
            System.out.print("[N]ew, ");
            System.out.print("[D]one, ");
            System.out.print("[P]ending, ");
            System.out.print("[C]ancelled, ");
            System.out.print("[U]pdate, ");
            System.out.print("[R]emove, ");
            System.out.print("[Q]uit: ");
            List<String> options = Arrays.asList("N", "D", "P", "C", "U", "R", "Q");
            Scanner in = new Scanner(System.in);
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

    private int getTaskId() {
        while (true) {
            try {
                System.out.print("Task ID (0 to quit): ");
                Scanner in = new Scanner(System.in);
                return in.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid option. Try again.");
            }
        }
    }

}
