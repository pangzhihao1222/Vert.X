package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;

public class UrlParamsVerticle extends AbstractVerticle {

  //第一步 声明router
  Router router;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //第二步 初始化Router
    router = Router.router(vertx);

    /**
     * 经典模式
     * http://localhost:8888/test?page=1&age=10
     */
    router.route("/test").handler(req->{
      //vert.x获取url参数就这一句req.request().getParam()
      var page = req.request().getParam("page");
      var age = req.request().getParam("age");
      req.response()
      .putHeader("content-type", "text/plain")
      .end(page+":"+age);
    });

    /**
     * rest风格
     * http://localhost:8888/test/1/10
     */
    router.route("/test/:page/:age").handler(req->
    {
      var page = req.request().getParam("page");
      var age = req.request().getParam("age");
      req.response()
      .putHeader("content-type", "text/plain")
      .end(page+":"+age);
    });


    //第三步 将Router与Vertx HttpServer 绑定
    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

}
