package vertx;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

import java.beans.BeanProperty;
import java.util.ArrayList;
import java.util.Properties;

public class ConfVerticle extends AbstractVerticle {
  //配置日志
  final InternalLogger logger = Log4JLoggerFactory.getInstance(MySqlVerticle.class);

  //第一步 声明router
  Router router;

  //配置连接参数
  MySQLConnectOptions connectOptions;

  //配置连接池
  PoolOptions poolOptions = new PoolOptions()
    .setMaxSize(5);


  MySQLPool client;


  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    /**
     * 引入配置依赖
     */
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    retriever.getConfig(ar -> {
      if (ar.failed()) {

      } else {
        JsonObject config = ar.result();

        connectOptions = new MySQLConnectOptions()
          .setPort(Integer.parseInt(config.getValue("port").toString()))
          .setHost(config.getString("host"))
          .setDatabase(config.getString("database"))
          .setUser(config.getString("user"))
          .setPassword(config.getString("password"));
        //创建连接数据库的客户端
        client = MySQLPool.pool(vertx, connectOptions, poolOptions);

        //第二步 初始化Router
        router = Router.router(vertx);

        //获取body参数，得先添加这句
        router.route().handler(BodyHandler.create());


        /**
         * 连接数据库
         */
        router.route("/test/list").handler(req -> {
          logger.error("进来了啊啊啊啊");
          Integer page = Integer.valueOf(req.request().getParam("page"));
          client.getConnection(ar1 -> {
            if (ar1.succeeded()) {
              System.out.println("Connected");
              SqlConnection conn = ar1.result();
              var offset = (page - 1) * 10;
              conn
                .preparedQuery("select * from vertx_demo limit 10 offset ?")
                .execute(Tuple.of(offset), ar2 -> {
                  conn.close();
                  if (ar2.succeeded()) {
                    var list = new ArrayList<JsonObject>();
                    ar2.result().forEach(item -> {
                      var json = new JsonObject();
                      json.put("id", item.getValue("id"));
                      json.put("name", item.getValue("name"));
                      list.add(json);
                    });
                    req.response()
                      .putHeader("content-type", "application/json")
                      .putHeader("charset", "utf-8")
                      .end(list.toString());
                  } else {
                    req.response()
                      .putHeader("content-type", "text/plain")
                      .end(ar2.cause().toString());
                  }
                });
            } else {
              System.out.println("Could not connect:" + ar1.cause().getMessage());
            }
          });

        });


        /**
         * json 格式
         * application/json
         * http://localhost:8888/test/json
         */
        router.route("/test/json").handler(req ->
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
    });


  }

}
