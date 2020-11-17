# Netty 网关
***
*学习性质的网关，此学习项目将逐步详细记录演进过程*

## 学习参考
&ensp;&ensp;&ensp;&ensp;自己也是第一次接触Netty，学习完以后感觉Netty是真的好用，这里将自己学习过程中的一些资料和学习路径记录下来，希望对大家有所启发

&ensp;&ensp;&ensp;&ensp;训练营中的讲述的方式是从底向上，如果之前没接触过，感觉还是写不出来，所以还是需要学习下其他资料

#### 极客时间 视频专栏 《Netty源码剖析与实战》
&ensp;&ensp;&ensp;&ensp;这个视频可以算是五星，作者讲解的很好，在这个专栏可以收获的是netty的相关知识、netty基本功能、netty
的扩展功能、阅读源码教学。这种是自顶向下，个人更喜欢这种方式吧，看完以后，有个大概的了解，例子写写，跑跑，大概也就半只脚进去了。

&ensp;&ensp;&ensp;&ensp;建议2-3倍速（Video Speed Control，可以用这个谷歌浏览器插件到3倍速）食用，1-3天可以刷完。第一遍主要是对它有个了解。这课质量还是有的，深挖还是需要下来下功夫。

&ensp;&ensp;&ensp;&ensp;学完以后心中差不多对netty能做什么有了了解了，想写，但还是写不出来，哈哈，我是这样的，需要看下面的。

#### 官方文档
- [User guide for 4.x](https://netty.io/wiki/user-guide-for-4.x.html
):这篇强烈推荐，仔细看完，将其中的代码自己敲一遍（不要复制，代码是手艺活，得亲自上手），搞完你对netty写一些简单的东西差不多就掌握了

- [Documentation](https://netty.io/wiki/index.html):总文档，API、变化等有空也可以点点，重要的是例子（建议直接拉取git仓库的源码，网页上看着有点不方便）
    - [Snoop](https://netty.io/4.1/xref/io/netty/example/http/snoop/package-summary.html) ‐ 构建自己的轻量级HTTP客户端和服务器,Http
    服务端和客户端示例，写网关的基础参考
    - Proxy：这个也在示例里面，代理的实现例子，对网关实现也有参考价值
    
    
    
## 工程说明
&ensp;&ensp;&ensp;&ensp;目前基本功能都已经实现，但在请求的细节上还有需要做的，目前只支持简单的字符串返回的后台服务器

- 网关服务端：接收用户请求
- 网关客户端：返回后台服务，得到响应数据
- 路由模块：解析服务端的请求地址，得到后台服务器对应地址，并对同一个服务器集群进行负载均衡
- 过滤模块：对请求和响应进行过滤处理
    
## 工程运行说明
- 网关程序入口：\src\main\java\com\gateway\GateWayApplication.java
- 后台服务程序入口：src\main\java\com\netty\example\helloworld\HttpHelloWorldServer
    
## 相关模块
&ensp;&ensp;&ensp;&ensp;当前的网关大体模块如下图：

![](https://github.com/lw1243925457/JAVA-000/blob/main/Week_03/gateway.png)
    
## 功能简介
&ensp;&ensp;&ensp;&ensp;目前系统分为四个模块：server模块、route模块、client模块、filter模块

- server模块：接收用户的请求，经过route模块解析后得到目标服务地址，client模块发送请求得到结果后，server返回给用户
- route模块：读取配置文件，加载路由配置，将不同的请求发送到不同的服务器
- client模块：发送请求到后台服务器，返回响应给server模块；目前集成了第三方异步非阻塞客户端和自写的同步非阻塞客户端
    - ThirdClientAsync:第三方异步非阻塞客户端
    - CustomClientAsync：自学的同步非阻塞，目前还不完善，某些bug导致响应没法返回，但正常运行的话，性能还行
- Filter模块：对请求和返回进行处理，内置将请求方法都设置为POST，返回头中添加GATEWAY信息

&ensp;&ensp;&ensp;&ensp;类似于NGINX，将用户请求根据配置转发到相应的后端服务程序中。目前还不支持restful json的请求。

&ensp;&ensp;&ensp;&ensp;配置示例：

```json5
{
  "server": {
    "group1": [
      "http://192.168.101.105:8080"
    ],
    "group2": [
      "http://192.168.101.105:8080",
      "http://192.168.101.109:8080"
    ]
  },
  "route": [
    {
      "source": "/greeting",
      "target": "group1"
    },
    {
      "source": "/hello",
      "target": "group2"
    }
  ]
}
```

&ensp;&ensp;&ensp;&ensp;目前采用前缀匹配，示例如下：

- localhost:80/greeting/getSome
    - 前缀匹配到 /greeting，得到转发目标机器机器为group1，则将发送请求到："http://192.168.101.105:8080",多个服务器的会就会轮询发送
    - 转发后端URL为："http://192.168.101.105:8080/getSome"
    
## 相关测试
### 测试运行说明
#### 后台服务器
- 后台服务器：本工程中的\src\main\java\com\netty\example\helloworld\HttpHelloWorldServer
    - 直接启动两个，分别监听在8080和8081端口
    
#### 网关
- 网关：两个测试，一个是集成了第三方异步非阻塞客户端，一个是集成了自己写的异步非阻塞客户端，在入口：GateWayApplication 中进行配置

```java
// 使用自定义第三方客户端
ClientCenter.getInstance().init(CUSTOM_CLIENT_ASYNC, clientGroup);

// 使用第三方客户端
ClientCenter.getInstance().init(THIRD_CLIENT_ASYNC, clientGroup);
```

### 测试结果
&ensp;&ensp;&ensp;&ensp;这里压测一下网关，基本命令如下，在2分钟左右基本能得到稳定值，不再大幅度抖动

```shell script
// 直接压测后台服务器
sb -u http://192.168.101.104:8080 -c 15 -N 120

// 通过网关压测一台后台服务器
sb -u http://192.168.101.104:81/group1/ -c 15 -N 120

// 通过网关压测两台后台服务器
sb -u http://192.168.101.104:81/group2/ -c 15 -N 120
```

&ensp;&ensp;&ensp;&ensp;得到的相关结果如下：

|测试条件说明                       |测试结果                          |
|----------                       |---------------------------------|
|不用网关直接访问单服务               | RPS: 3860 (requests/second)   |
|经过网关访问单服务(自定义客户端）                   | RPS: 3255.2 (requests/second)  |
|经过网关访问两个服务器(自定义客户端）（负载均衡）     | RPS: 3347.4 (requests/second)   |
|经过网关访问单服务(第三方客户端）                   | RPS: 3288.5 (requests/second)  |
|经过网关访问两个服务器(第三方客户端）（负载均衡）     | RPS: 3297.4 (requests/second)   |

&ensp;&ensp;&ensp;&ensp;经过上面的测试数据可以发现，经过网关性能是要差一些的。感觉这样应该是正常的，毕竟网络链路都要多走一步。

&ensp;&ensp;&ensp;&ensp;如果后端服务的host和port相同的话，那就相当于代理了，经过测试，如果简单代理的话，性能几乎是相同的。

&ensp;&ensp;&ensp;&ensp
;经过网关访问两个服务器（负载均衡）的测试结果不是预料中的，想象中应该是两倍的性能，但这里要考虑到网关的性能是否能够支撑了。由于机器的性能基本上已经打满了，这里就没法去测试这个准确的。但可以看到相对于单服务器，两个服务器的性能是有所提升的。

&ensp;&ensp;&ensp;&ensp;负载均衡的效果不是理想中的，后面有条件再尝试尝试。但其他的应该是够了

## 改动记录
### V1.0
#### 版本功能
- 将 localhost:80/ 转到 localhost:8080/，并返回结果

#### 工程结构
- client：此模块负责将请求目标服务
- server：负责接收用户请求

#### 代码说明
&ensp;&ensp;&ensp;&ensp;代码思路大致如下图：

![](./picture/v1.png)

&ensp;&ensp;&ensp;&ensp;网关流程：用户发起请求，server端接收到请求，将请求转给client，client发送请求给内部服务，得到结果以后将结果返回给用户。

&ensp;&ensp;&ensp;&ensp;代码编写中一个难点是，server端和client端的结合。这里采用的是，在server端处理请求的时候，将当前channel传递到client handler中，这样client
 handle得到结果后，直接使用server channel返回结果即可。代码比较少也比较简单，这里就不贴了。
 
### V1.1
#### 更新说明
- 添加路由配置：com.gateway.util.Config
- 添加路由转发: com.gateway.route.RouteTableSingleton

#### 代码说明
- com.gateway.util.Config: 读取properties配置文件
- com.gateway.route.RouteTableSingleton：读取配置生成路由转发表

### V1.2
#### 更新说明
&ensp;&ensp;&ensp;&ensp;这个版本进行压测和调整，代码在最开始时稳定性不行，在RPS在1700左右程序就会崩溃

&ensp;&ensp;&ensp;&ensp;经过再三的调整和尝试，稳定性解决了，但在之前的代码架构下RPS只在700左右

&ensp;&ensp;&ensp;&ensp;在v1.0的架构下，Client Channel需要保留当前的Server outbound，已便于在获取结果后返回数据给用户，所有Client是有状态的

&ensp;&ensp;&ensp;&ensp;但目前网关需要请求不同的后台服务，服务的地址和端口可能不一样，这样Client Channel无法复用，导致Client线程的频繁创建和销毁，严重影响了网关性能

&ensp;&ensp;&ensp;&ensp;想到的解决办法就是使用Client同步非阻塞请求，Client channel 不与Server outbound进行绑定，这样实现了解耦和Client的线程复用

&ensp;&ensp;&ensp;&ensp;但同步非阻塞客户端自己目前实现有点困难，就使用了一个第三方的，起码效果看起来比之前好多了，后面自己再仿照写一个试试

&ensp;&ensp;&ensp;&ensp;下面是各个改动的性能说明：

- 通过网关，直接访问服务：5000左右的RPS
- 通过网关，但没有路由模块，也就是直接代理：5000左右的RPS
- 原始版本：在RPS在1700左右崩溃
- 改动版本：稳定性可以，但RPS只有700左右
- 同步非阻塞客户端，通过路由模块：4200左右的RPS

#### 代码说明
&ensp;&ensp;&ensp;&ensp;Client替换为第三方的异步客户端，直接在Server Handler中获取服务器请求后返回

&ensp;&ensp;&ensp;&ensp;异步客户端依赖：

```java
implementation "org.asynchttpclient:async-http-client:2.12.1"
```

&ensp;&ensp;&ensp;&ensp;异步客户端的简单使用：

```java
public class ClientTest {

    @Test
    public void asyncClientTest() throws ExecutionException, InterruptedException {
        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        Future<Response> responseFuture = asyncHttpClient.prepareGet("http://192.168.101.105:8080/").execute();
        Response response = responseFuture.get();
        System.out.println(response.toString());
        System.out.println(response.getStatusCode());
        System.out.println(response.getHeaders().toString());
        System.out.println(response.getResponseBody().toString().getBytes());
    }
}
```

### V1.3
#### 更新说明
&ensp;&ensp;&ensp;&ensp;添加Request和Response的过滤处理，内置实现了将Request的方法都设置为POST，将Response的Header中增加信息

#### 代码说明
- FilterSingleton、Filter：过滤链配置保存，将相应的Response和Request过滤实现类添加到处理链，在过滤处理时就逐步调用类方法进行处理，有点类似Netty的Pipeline

```java
/**
 * 过滤器
 * @author lw
 */
public class Filter {
    static final FilterSingleton filterSingleton = FilterSingleton.getInstance();

    static private void addRequestFilter(RequestFilter requestFrontFilter) {
        filterSingleton.registerRequestFrontFilter(requestFrontFilter);
    }

    static private void addResponseFilter(ResponseFilter responseBackendFilter) {
        filterSingleton.registerResponseBackendFilter(responseBackendFilter);
    }

    /**
     * 在这个方法中添加Request的过滤操作类,在启动函数中进行调用
     */
    static public void initRequestFilter() {
        addRequestFilter(new MethodToPost());
    }

    /**
     * 在这个方法中添加Response的过滤操作类，在启动函数中进行调用
     */
    static public void initResponseFilter() {
        addResponseFilter(new AddGatewayInfo());
    }

    /**
     * 遍历Request过滤操作链，对Request进行处理，在Server inbound接收到Request后进行调用
     * @param request
     */
    static public void requestProcess(HttpRequest request) {
        for (RequestFilter filter: filterSingleton.getRequestFrontFilterList()) {
            filter.filter(request);
        }
    }

    /**
     * 调用Response过滤操作链，对Response进行处理，在Server outbound发送Response前进行调用
     * @param response
     */
    static public void responseProcess(HttpResponse response) {
        for (ResponseFilter filter: filterSingleton.getResponseBackendFilters()) {
            filter.filter(response);
        }
    }
}
```

- RequestFilter：Request过滤处理器接口，实现此接口对Request进行处理
- ResponseFilter：Response过滤处理器接口，实现此接口对Response进行处理

### V1.4
#### 更新说明
- 加入负载均衡：使用基本的轮询算法
- 重构路由配置：使用JSON配置文件

#### 代码说明
- LoadBalance：负载均衡算法接口
- Rotation：轮询负载均衡算法

### V1.5
#### 更新说明
- 添加自写的同步非阻塞客户端

#### 代码说明
&ensp;&ensp;&ensp;&ensp;这个版本更新主要是解决下面两个问题：

- 自写的异步非阻塞客户端：前面用的是第三方写的，不用自己写的就有点怪异
- 网关客户端和服务端的解耦：server outbound 和 client inbound 不应该同时处于客户端和服务端的handler中，这样不利于后面的扩展

##### 自写异步客户端关键代码说明
&ensp;&ensp;&ensp;&ensp;客户端的关键有两点：

- 1.能从客户端的返回结果：netty都是异步的，这里采用锁的等待-通知机制，将结果返回。
- 2.网关客户端和服务端的解耦：这里使用客户端中心，通过传入的server outbound将客户端返回的响应直接返回

&ensp;&ensp;&ensp;&ensp;后台服务器响应的获取，直接从最后的handler中做文章，从里面获取结果，相关代码如下：

```java
/**
 * 这里使用并发的等待-通知机制来拿到结果
 * @author lw
 */
public class CustomClientAsyncHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private CountDownLatch latch;
    private FullHttpResponse response;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        // 拿到结果后再释放锁
        response = CreatResponse.createResponse(msg);
        latch.countDown();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 锁的初始化
     * @param latch CountDownLatch
     */
    void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    /**
     * 阻塞等待结果后返回
     * @return 后台服务器响应
     * @throws InterruptedException
     */
    public FullHttpResponse getResponse() throws InterruptedException {
        latch.await();
        return response;
    }
}
```

&ensp;&ensp;&ensp;&ensp;因为pipeline是可以动态变化的，我们在初始化的时候，只添加前面的编解码即可，当需要发起请求给后台服务器的时候才装载

```java
// 每次发起请求都new一个新的handler，并重置CountDownLatch
CustomClientAsyncHandler handler = new CustomClientAsyncHandler();
handler.setLatch(new CountDownLatch(1));
// 获取client outbound
Channel channel = createChannel(address, port);
// 在pipeline最后添加上面的handler
channel.pipeline().addLast("clientHandler", handler);
```

&ensp;&ensp;&ensp;&ensp;其中一个关键是channel的复用，当用户请求和后台服务器相同时，我们能复用之前的channel，那是非常关键的

&ensp;&ensp;&ensp;&ensp;channel的复用这里暂时采取用ConcurrentHashMap<Channel, Channel>，来存取服务端和对应的客户端

&ensp;&ensp;&ensp;&ensp;复用的时候，需要移除之前的handler，重新再添加一个handler(应该是重置后可以复用，但有点小问题没解决)

```java
// 移除之前的handler
channel.pipeline().removeLast();
// 新建handler并设置锁
CustomClientAsyncHandler handler = new CustomClientAsyncHandler();
handler.setLatch(new CountDownLatch(1));
// 添加handler
channel.pipeline().addLast("clientHandler", handler);
```

&ensp;&ensp;&ensp;&ensp;解耦相对就比较简单了，新建一个ClientCenter来承接就行了，这里就不多介绍了，相关代码中execute函数中

```java
/**
 * 使用时请进行初始化操作
 * 客户端中心
 * 起一个中介中用，获取后台服务器结果，调用server outbound返回结果
 * @author lw
 */
public class ClientCenter {

    public void execute(FullHttpRequest request, Channel serverOutbound) {
        // 路由转发处理,负载均衡
        String source = request.uri();
        String target = RouteTable.getTargetUrl(source);

        URI uri = null;
        try {
            uri = new URI(target);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        String address = uri.getHost();
        int port = uri.getPort();
        request.setUri(uri.getPath());

        // 请求过滤处理
        Filter.requestProcess(request);

        FullHttpResponse response = client.execute(request, address, port, serverOutbound);
        if (response == null) {
            System.out.println("backend server return null");
        }

        // 相应过滤处理
        Filter.responseProcess(response);

        // 返回Response数据给用户
        try {
            serverOutbound.writeAndFlush(response).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

 
## TODO LIST
- 1. 10-讲网关的frontend/backend/filter/router/线程池都改造成Spring配置方式；
- 2. 20-基于AOP改造Netty网关，filter和router使用AOP方式实现；
- 3. 30-基于前述改造，将网关请求前后端分离，中级使用JMS传递消息；
- 4. 输出改log规范化

 
## 参考链接
- [Java Properties file examples](https://mkyong.com/java/java-properties-file-examples/)
- [google/guava](https://github.com/google/guava)
- [Netty中的基本组件及关系](https://blog.csdn.net/summerZBH123/article/details/79344226)
- [netty4客户端连接多个不同的服务端](https://blog.csdn.net/zsj777/article/details/102726029)
- [Netty 中的 handler 和 ChannelPipeline 分析](https://www.cnblogs.com/rickiyang/p/12686593.html)
- [一个简单可参考的API网关架构设计](https://www.infoq.cn/article/api-gateway-architecture-design)
- [AsyncHttpClient/async-http-client](https://github.com/AsyncHttpClient/async-http-client)
- [Netty中ChannelHandler共享数据的方式](https://blog.csdn.net/u013721793/article/details/51204029)
- [In Netty 4, what's the difference between ctx.close and ctx.channel.close?](https://stackoverflow.com/questions/21240981/in-netty-4-whats-the-difference-between-ctx-close-and-ctx-channel-close)
- [How to write a high performance Netty Client](https://stackoverflow.com/questions/8444267/how-to-write-a-high-performance-netty-client)

### 网关
- [如何设计一个亿级网关(API Gateway)](http://woshinlper.com/system-design/micro-service/API%E7%BD%91%E5%85%B3/)
- [聊聊 API Gateway 和 Netflix Zuul](http://www.scienjus.com/api-gateway-and-netflix-zuul/)
- [有赞API网关实践](https://tech.youzan.com/api-gateway-in-practice/)
- [Leo|20页PPT剖析唯品会API网关设计与实践](https://mp.weixin.qq.com/s/gREMe-G7nqNJJLzbZ3ed3A)
- [API网关性能比较：NGINX vs. ZUUL vs. Spring Cloud Gateway vs. Linkerd](https://www.infoq.cn/article/comparing-api-gateway-performances)

### Netty
- [Netty 源码解析（三）: Netty 的 Future 和 Promise](https://juejin.im/post/6844904144793419784)

### 多线程
- [线程池最佳实践！安排！](https://juejin.im/post/6844904186400899086)
- [JAVA 拾遗 --Future 模式与 Promise 模式](https://www.cnkirito.moe/future-and-promise/)

