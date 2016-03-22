package vertx_task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class Todo_main {

	private Vertx vertx;

	  @Before
	  public void setUp(TestContext context) {
	    vertx = Vertx.vertx();
	    vertx.deployVerticle(App.class.getName(),
	        context.asyncAssertSuccess());
	  }

	  @After
	  public void tearDown(TestContext context) {
	    vertx.close(context.asyncAssertSuccess());
	  }
	  
	  @Test
	  public void runMyApplication(TestContext context) {
	    final Async async = context.async();

	    vertx.createHttpClient().getNow(8080, "localhost", "/",
	     response -> {
	      response.handler(body -> {
	        context.assertTrue(body.toString().contains("Hello"));
	        async.complete();
	      });
	    });
	  }
}
