package org.zyj.hulk.net.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

/**
 * @author zhangyj
 * @date 2022/1/26 19:40
 */
@Configuration
@Order(Integer.MAX_VALUE)
@Slf4j
public class NettyServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    @Autowired
    private DispatcherHandler dispatcherHandler;

    @Autowired
    private LogHandler logHandler;

    private boolean isRunning;

    @PostConstruct
    public void init() {
        this.bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss-"));
        this.workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() + 1, new DefaultThreadFactory("IO-"));

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("log", logHandler)
                                .addLast("fixLength", new FixedLengthFrameDecoder(20))
                                .addLast("decoder", new StringDecoder(Charset.forName("utf8")))
                                .addLast("encoder", new StringEncoder(Charset.forName("utf8")))
                                .addLast("dispatcherHandler", dispatcherHandler);
                    }
                });

        ChannelFuture future = serverBootstrap.bind(7777).awaitUninterruptibly();
        future.channel().closeFuture().addListener(f -> {
            stop();
        });
        if (future.cause() != null)
            log.error("启动失败", future.cause());

        if (future.isSuccess())
            log.warn("==={}启动成功,port:{}===", "tcpServer", 7777);
    }

    public synchronized void stop() {
        isRunning = false;
        bossGroup.shutdownGracefully();
        if (workerGroup != null)
            workerGroup.shutdownGracefully();

        log.warn("==={}已经停止,port:{}===", "tcpServer", 7777);
    }
}
