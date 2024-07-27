package com.dmslabbert.taskmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskMasterApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(TaskMasterApplication.class, args);
		TaskMasterService service = context.getBean(TaskMasterService.class);
		service.run();
		context.close();
	}

}
