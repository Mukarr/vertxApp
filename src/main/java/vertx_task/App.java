package vertx_task;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.netty.handler.codec.http.HttpMethod;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class App extends AbstractVerticle {

	// Store our tasks
	private Map<Integer, Task> tasks = new LinkedHashMap<>();

	// Create some tasks
		
	private void createSomeData() {
		Task dothis = new Task("GSoC Proposal");
		tasks.put(dothis.getId(), dothis);
		Task dothat = new Task("Ionic install");
		tasks.put(dothat.getId(), dothat);
	}

	private void getAll(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(tasks.values()));
	}

	private void addOne(RoutingContext routingContext) {
		JsonObject json = routingContext.getBodyAsJson();
		System.out.println(json);
		Task task = new Task(json.getString("name"));
		tasks.put(task.getId(), task);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(task));
	}

	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			tasks.remove(idAsInteger);
		}
		routingContext.response().setStatusCode(204).end();
	}

	private void updateOne(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		JsonObject json = routingContext.getBodyAsJson();
		if (id == null || json == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			final Integer idAsInteger = Integer.valueOf(id);
			Task task = tasks.get(idAsInteger);
			if (task == null) {
				routingContext.response().setStatusCode(404).end();
			} else {
				
				task.setName(json.getString("name"));
				task.setCompleted(json.getString("completed").equals("true")?true:false);
				task.setPriority(Integer.parseInt(json.getString("priority")));
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(task));
			}
		}
	}

	@Override
	public void start(Future<Void> fut) {

		// creating some data
		createSomeData();

		// Create a router object.
		Router router = Router.router(vertx);
		
		// CORS support
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("origin");
        allowHeaders.add("content-type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);
		
		// Serve static resources from the /assets directory
		router.route("/assets/*").handler(StaticHandler.create("assets"));

		router.get("/api/tasks").handler(this::getAll);
		router.route("/api/tasks*").handler(BodyHandler.create());
		router.post("/api/tasks").handler(this::addOne);
		router.delete("/api/tasks/:id").handler(this::deleteOne);
		router.put("/api/tasks/:id").handler(this::updateOne);

		// Bind "/" to our hello message - so we are still compatible.
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html")
					.end("<h1>For seeing my first Vert.x 3 application using Vert.x Web visit port</h1>" + "http.port");
		});

		// Create the HTTP server and pass the "accept" method to the request
		// handler.
		vertx.createHttpServer().requestHandler(router::accept).listen(
				// Retrieve the port from the configuration,
				// default to 8080.
				Integer.getInteger("http.port", 8080), System.getProperty("http.address","localhost"));

	}
}