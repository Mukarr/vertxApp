package vertx_task;

import java.util.LinkedHashMap;
import java.util.Map;

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

	// Store our product
	private Map<Integer, Task> products = new LinkedHashMap<>();

	// Create some product
	private void createSomeData() {
		Task bowmore = new Task("GSoC Proposal");
		products.put(bowmore.getId(), bowmore);
		Task talisker = new Task("Ionic install");
		products.put(talisker.getId(), talisker);
	}

	private void getAll(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(products.values()));
	}

	private void addOne(RoutingContext routingContext) {
		JsonObject json = routingContext.getBodyAsJson();
		System.out.println(json);
		Task task = new Task(json.getString("name"));
		products.put(task.getId(), task);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(task));
	}

	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			products.remove(idAsInteger);
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
			Task task = products.get(idAsInteger);
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
				config().getInteger("http.port", 8080), result -> {
					if (result.succeeded()) {
						fut.complete();
					} else {
						fut.fail(result.cause());
					}
				});

	}
}