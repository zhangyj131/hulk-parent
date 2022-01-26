package org.zyj.hulk.net;

import io.netty.bootstrap.AbstractBootstrap;
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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyj
 * @date 2022/1/25 0:46
 */

@Slf4j
@Order(Integer.MIN_VALUE)
@Configuration
//@ConditionalOnProperty(value = "jt-server.jt808.enable", havingValue = "true")
//@ConditionalOnProperty(value = "jt-server.jt808.port.tcp")
//@Bean(initMethod = "start", destroyMethod = "stop")
public class TCPServer implements InitializingBean, DisposableBean {
    private volatile boolean isRunning;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ExecutorService businessGroup;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.isRunning) {
            log.warn("==={}已经启动,port:{}===", "nettyServer", 7001);
        } else {
            this.bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("TCPServer-boss", 10));
            this.workerGroup = new NioEventLoopGroup(2, new DefaultThreadFactory("TCPServer-IO", 10));
            this.businessGroup = new ThreadPoolExecutor(10, 10, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new DefaultThreadFactory("TCPServer" + "-B", true, 5));
            AbstractBootstrap bootstrap = initialize();
            ChannelFuture future = bootstrap.bind(7001).awaitUninterruptibly();
            future.channel().closeFuture().addListener(f -> {
                if (isRunning) destroy();
            });
            if (future.cause() != null)
                log.error("启动失败", future.cause());

            if (isRunning = future.isSuccess())
                log.warn("==={}启动成功,port:{}===", "TCPServer", 7001);
        }
    }

    @Override
    public void destroy() throws Exception {
        isRunning = false;
        bossGroup.shutdownGracefully();
        if (workerGroup != null)
            workerGroup.shutdownGracefully();
        if (businessGroup != null)
            businessGroup.shutdown();
        log.warn("==={}已经停止,port:{}===", "TCPServer", 7001);
    }

    protected AbstractBootstrap initialize() {
        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    public void initChannel(NioSocketChannel channel) {
                        channel.pipeline()
//                                .addLast(new IdleStateHandler(180, 180, 180))
                                .addLast("frameDecoder", new FixedLengthFrameDecoder(10))
                                .addLast("decoder", new StringDecoder())
                                .addLast("encoder", new StringEncoder());
                    }
                });
    }
}
