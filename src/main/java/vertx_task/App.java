package vertx_task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.netty.handler.codec.http.HttpMethod;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

public class App extends AbstractVerticle {

	// Store our tasks
//	private Map<Integer, Task> products = new LinkedHashMap<>();
	private static final String Todo = "TODO";
	RedisClient redis;
	
	// Create some tasks
	private void createSomeData() {
		
		final RedisOptions config = new RedisOptions()
                .setHost("127.0.0.1");
        redis = RedisClient.create(vertx, config);
        
        Task dothis = new Task("GSoC Proposal");
        redis.hset(Todo, String.valueOf(dothis.getId()), Json.encode(
                dothis), res -> {
            if (res.failed()) {
                System.out.println("Redis not available");
            }else{
            	System.out.println("Redix working");
            }
        });
        
        Task dothat = new Task("Ionic install");
        redis.hset(Todo, String.valueOf(dothat.getId()), Json.encode(
                dothat), res -> {
            if (res.failed()) {
                System.out.println("Redis not available");
            }
        });
		
//		
//		Task dothis = new Task("GSoC Proposal");
//		products.put(dothis.getId(), dothis);
//		Task dothat = new Task("Ionic install");
//		products.put(dothat.getId(), dothat);
	}

	private void getAll(RoutingContext routingContext) {
		
		redis.hgetall(Todo, res -> {
            List<Object> list = new ArrayList<>();
            if (res.succeeded()) {
                res.result().forEach(x ->
                        list.add(Json.decodeValue((String)x.getValue(), Task.class)));
                String encoded = new JsonArray(list).encode();
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(encoded);
            }
            else
            	routingContext.response()
                .setStatusCode(404);
                
        });
		
//		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
//				.end(Json.encodePrettily(products.values()));
	}

	private void addOne(RoutingContext routingContext) {
		JsonObject json = routingContext.getBodyAsJson();
		System.out.println(json);
		Task task = new Task(json.getString("name"));
		
		redis.hset(Todo, String.valueOf(task.getId()),
				Json.encode(task), res -> {
                    if (res.succeeded())
                        routingContext.response()
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encode(task));
                    else
                    	routingContext.response().setStatusCode(503).end();
                });
		
		
//		products.put(task.getId(), task);
//		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
//				.end(Json.encodePrettily(task));
	}

	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		System.out.println(id);
			redis.hdel(Todo, id, res -> {
	            if (res.succeeded())
	                routingContext.response().setStatusCode(204).end();
	            else
	                routingContext.response().setStatusCode(404).end();
	        });
	    
		
//			Integer idAsInteger = Integer.valueOf(id);
//			products.remove(idAsInteger);
	}
	
	private void deleteAll(RoutingContext routingContext){
		
		redis.del(Todo, res -> {
            if (res.succeeded())
                routingContext.response().setStatusCode(204).end();
            else
                routingContext.response().setStatusCode(404).end();
        });
	}

	private void updateOne(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		JsonObject json = routingContext.getBodyAsJson();
		if (id == null || json == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
		     redis.hget(Todo, id, x -> {
		            String result = x.result();
		            if (result == null)
		            	routingContext.response().setStatusCode(404).end();
		            else {  	
		            	Task task = Json.decodeValue(result, Task.class);
		            	task.setName(json.getString("name"));
						task.setCompleted(json.getString("completed").equals("true")?true:false);
						task.setPriority(Integer.parseInt(json.getString("priority")));
		                String response = Json.encode(task);
		                redis.hset(Todo, id, response, res -> {
		                    if (res.succeeded()) {
		                    	routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .end(response);
		                    }
		                });
		            }
		        });
/*
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
*/
		}
	}

	public void start(Future<Void> fut) throws Exception{

		System.out.println("I came here");
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
					.end("<h1>For seeing my first Vert.x 3 application using Vert.x Web visit /assets");
		});

		// Create the HTTP server and pass the "accept" method to the request
		// handler.
		vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(8082, result -> {
		          if (result.succeeded()) {
		            fut.complete();
		          } else {
		            fut.fail(result.cause());
		          }
		        });
				// Retrieve the port from the configuration,
				// default to 8080.
				//Integer.getInteger("http.port", 8080), System.getProperty("http.address","localhost"));
	}
}