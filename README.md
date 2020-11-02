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
    
## 功能简介
&ensp;&ensp;&ensp;&ensp;目前系统分为三个模块：server模块、route模块、client模块

- server模块：接收用户的请求，经过route模块解析后得到目标服务地址，client模块发送请求得到结果后，server返回给用户
- route模块：读取配置文件，加载路由配置，将不同的请求发送到不同的服务器
- client模块：异步请求客户端，返回请求结果给server模块

&ensp;&ensp;&ensp;&ensp;类似于NGINX，将用户请求根据配置转发到相应的后端服务程序中。目前还不支持restful json的请求。

&ensp;&ensp;&ensp;&ensp;配置示例：

```$xslt
# 定义后端服务列表
route.rule.hots = host1 host2

# 定义host1后端服务的地址和端口
route.rule.hosts.host1.ip = 192.168.101.105
route.rule.hosts.host1.port = 8080

# 定义host2后端服务的地址和端口
route.rule.hosts.host2.ip = localhost
route.rule.hosts.host2.port = 8081
```

&ensp;&ensp;&ensp;&ensp;目前采用前缀匹配，示例如下：

- localhost:80/host2/greeting 
    - 前缀为host2，得到转发目标机器地址和端口：localhost 8081
    - 转发后端URL为：localhost:8081/greeting
    
&ensp;&ensp;&ensp;&ensp;目前性能上稍有欠缺，但稳定性比以前好上很多，下面是性能对比

- 1.不通过网关，直接访问服务：5000左右的RPS
- 2.通过网关，但没有路由模块，也就是直接代理：5000左右的RPS
- 3.通过网关，有路由模块：4200左右的RPS

&ensp;&ensp;&ensp;&ensp;性能稍差的原因：1和2代码都能做到Channel是一个无状态的，也会能直接复用，经过测试起到4-8个Channel线程就能hold。

&ensp;&ensp;&ensp;&ensp;但3的情况，很多情况下需要保留当前的server outbound的状态，因为后台服务不是一个，所有大部分情况下不能复用，频繁的销毁和启动线程，导致性能大幅度下降。

&ensp;&ensp;&ensp;&ensp;目前程序client采用的是异步请求客户端（github开源的，后面自己写一个试试），与server模块解耦。经过的链路和处理也比较多，虽然性能下降是正常，但感觉还是有点多，再调整试试

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
 
## TODO
- 过滤模块的编写：对用户请求的前置处理和后置处理
- 异步客户端尝试编写
 
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

