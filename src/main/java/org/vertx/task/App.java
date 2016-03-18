package org.vertx.task;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

public class App extends AbstractVerticle {

	@Override
	public void start(Future<Void> fut) {
		/**
		 * using vert.x-core vertx.createHttpServer().requestHandler(r -> {
		 * r.response().end("<h1>Hello from my first " +
		 * "Vert.x 3 application</h1>"); }).listen( //get port from
		 * configuration file //default:8080 config().getInteger("http.port",
		 * 8080), result -> { if (result.succeeded()) { fut.complete(); } else {
		 * fut.fail(result.cause()); } });
		 **/
		/** Now Using vert.x-web **/

		// Create a router object.
		Router router = Router.router(vertx);

		// Bind "/" to our hello message - so we are still compatible.
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html").end("<h1>Hello from my first Vert.x 3 application using Vert.x Web</h1>");
		});

		// Create the HTTP server and pass the "accept" method to the request handler.
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