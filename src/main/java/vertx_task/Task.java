package vertx_task;

import java.util.concurrent.atomic.AtomicInteger;

public class Task {

	private static final AtomicInteger COUNTER = new AtomicInteger();
	
	private static final AtomicInteger COUNT = new AtomicInteger();

	private final int id;

	private String title;
	
	private Boolean completed;
	
	private String url;
	
	private int order;

	public Task(String title) {
		this.id = COUNTER.incrementAndGet();
		this.title = title;
		this.completed = false;
		this.order = COUNT.incrementAndGet();
	}
	
	public Task() {
		this.id = COUNTER.getAndIncrement();
	}

	public Task(String title, Boolean completed, int order, String url) {
		this.id = COUNTER.getAndIncrement();
        this.title = title;
        this.completed = completed;
        this.order = order;
        this.url = url;
    }
	
	public Task(String title, int order) {
		this.id = COUNTER.getAndIncrement();
		this.title =title;
		this.order = order;
	}
	
	public String getTitle() {
		return title;
	}

	public int getOrder() {
		return order;
	}

	public int getId() {
		return id;
	}
	
	public String getUrl(){
		return url;
	}
	
	public Boolean getCompleted(){
		return completed;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setOrder(int order) {
		this.order = order;
	}
	
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	
	public void setUrl(String url){
		this.url=url;
	}
	
}