package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class BodyVerticle extends AbstractVerticle {

  //第一步 声明router
  Router router;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //第二步 初始化Router
    router = Router.router(vertx);

    //获取body参数，得先添加这句
    router.route().handler(BodyHandler.create());

    // 第四步 配置Router解析url
//    router.route("/").handler(req->{req.response()
//      .putHeader("content-type", "text/plain")
//      .end("Hello from Vert.x!");
//  });
    /**
     * form-data 格式
     * application/x-www-form-urlencoded
     * http://localhost:8888/test/form
     */
    router.route("/test/form").handler(req->{
      //vert.x获取form-data参数就这一句req.request().getFormAttribute()
     var page = req.request().getFormAttribute("page");
     var age = req.request().getFormAttribute("age");
      req.response()
        .putHeader("content-type", "text/plain")
        .end(page+":"+age);
    });

    /**
     * json 格式
     * application/json
     * http://localhost:8888/test/json
     */
    router.route("/test/json").handler(req->
    {
      //vert.x获取json参数就这一句req.getBodyAsJson()
      var page = req.getBodyAsJson();
      req.response()
        .putHeader("content-type", "text/plain")
        .end(page.toString());
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
