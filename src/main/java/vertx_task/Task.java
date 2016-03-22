package vertx_task;

import java.util.concurrent.atomic.AtomicInteger;

public class Task {

	private static final AtomicInteger COUNTER = new AtomicInteger();
	
	private static final AtomicInteger COUNT = new AtomicInteger();

	private final int id;

	private String name;
	
	private Boolean completed;
	
	private int priority;

	public Task(String name) {
		this.id = COUNTER.incrementAndGet();
		this.name = name;
		this.completed = false;
		this.priority = COUNT.incrementAndGet();
	}

	public Task() {
		this.id = COUNTER.getAndIncrement();
	}

	public String getName() {
		return name;
	}

	public int getPriority() {
		return priority;
	}

	public int getId() {
		return id;
	}
	
	public Boolean getCompleted(){
		return completed;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	
}