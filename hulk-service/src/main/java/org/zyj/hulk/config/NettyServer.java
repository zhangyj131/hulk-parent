package org.zyj.hulk.config;

import io.netty.bootstrap.ServerBootstrap;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyj
 * @date 2022/1/26 19:40
 */
@Configuration
@Order(Integer.MAX_VALUE)
public class NettyServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;
    private ExecutorService businessGroup;
    private DispatcherHandler dispatcherHandler;

    @PostConstruct
    public void init() {
        this.bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss-"));
        this.workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() + 1, new DefaultThreadFactory("IO-"));
        this.businessGroup = new ThreadPoolExecutor(200, 200, 300L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory("biz-"));

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("fixLength", new FixedLengthFrameDecoder(20))
                                .addLast("decoder", new StringDecoder(Charset.forName("utf8")))
                                .addLast("dispatcherHandler", dispatcherHandler)
                                .addLast("encoder", new StringEncoder());
                    }
                });


    }
}
