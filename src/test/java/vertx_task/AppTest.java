package vertx_task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import vertx_task.App;

@RunWith(VertxUnitRunner.class)
public class AppTest {

  private Vertx vertx;
private int port;

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
    port = 8081;
    DeploymentOptions options = new DeploymentOptions()
        .setConfig(new JsonObject().put("http.port", port)
    );
    vertx.deployVerticle(App.class.getName(),options,context.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testMyApplication(TestContext context) {
    final Async async = context.async();

    vertx.createHttpClient().getNow(port, "localhost", "/",
     response -> {
      response.handler(body -> {
        //context.assertTrue(body.toString().contains("Hello"));
        async.complete();
      });
    });
  }
}