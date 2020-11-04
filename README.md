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
    
    
    
    
## 相关模块
&ensp;&ensp;&ensp;&ensp;当前的网关大体模块如下图：

![](https://github.com/lw1243925457/JAVA-000/blob/main/Week_03/gateway.png)
    
## 功能简介
&ensp;&ensp;&ensp;&ensp;目前系统分为四个模块：server模块、route模块、client模块

- server模块：接收用户的请求，经过route模块解析后得到目标服务地址，client模块发送请求得到结果后，server返回给用户
- route模块：读取配置文件，加载路由配置，将不同的请求发送到不同的服务器
- client模块：同步非阻塞请求客户端，返回请求结果给server模块；目前使用第三方提供的，自己实现的性能不行
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
&ensp;&ensp;&ensp;&ensp;这里压测一下网关，基本命令如下，在2分钟左右基本能得到稳定值，不再大幅度抖动

```shell script
sb -u http://localhost:80/greeting -c 20 -N 120
```

&ensp;&ensp;&ensp;&ensp;得到的相关结果如下：

|测试条件说明                       |测试结果                          |
|----------                       |---------------------------------|
|不用网关直接访问单服务               | RPS: 5887.5 (requests/second)   |
|经过网关访问单服务                   | RPS: 5191.9 (requests/second)  |
|经过网关访问两个服务器（负载均衡）     | RPS: 5664.5 (requests/second)   |

&ensp;&ensp;&ensp;&ensp;经过上面的测试数据可以发现，经过网关性能是要差一些的。感觉这样应该是正常的，毕竟网络链路都要多走一步。

&ensp;&ensp;&ensp;&ensp;如果后端服务的host和port相同的话，那就相当于代理了，经过测试，如果简单代理的话，性能几乎是相同的。

&ensp;&ensp;&ensp;&ensp;目前网关假设是后端服务会有不同的ip地址和端口，所以Server端测试的时候线程新建和销毁比代理要多，而且客户端必须是异步的，有状态的客户端会导致更多的线程新建和销毁。

&ensp;&ensp;&ensp;&ensp
;经过网关访问两个服务器（负载均衡）的测试结果不是预料中的，想象中应该是两倍的性能，但这里要考虑到网关的性能是否能够支撑了。由于机器的性能基本上已经打满了，这里就没法去测试这个准确的。但可以看到相对于单服务器，两个服务器的性能是有所提升的。

&ensp;&ensp;&ensp;&ensp;目前来看功能上是达到作业要求了，但性能上可能有些不足。做下来感觉网关这个东西还有很多很多的点，这里只是一小部分，不简单。




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

&ensp;&ensp;&ensp;&ensp;想到的解决办法就是使用Client异步请求，Client channel 不与Server outbound进行绑定，这样实现了解耦和Client的线程复用

&ensp;&ensp;&ensp;&ensp;但异步客户端自己目前实现有点困难，就使用了一个第三方的，起码效果看起来比之前好多了，后面自己再仿照写一个试试

&ensp;&ensp;&ensp;&ensp;下面是各个改动的性能说明：

- 通过网关，直接访问服务：5000左右的RPS
- 通过网关，但没有路由模块，也就是直接代理：5000左右的RPS
- 原始版本：在RPS在1700左右崩溃
- 改动版本：稳定性可以，但RPS只有700左右
- 异步客户端，通过路由模块：4200左右的RPS

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
 
## TODO
- 由于是使用第三方的异步客户端，过滤模块没有很好的结合起来，需要尝试自己仿写一个
- 补习相关知识，进一步完善网关
 
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

