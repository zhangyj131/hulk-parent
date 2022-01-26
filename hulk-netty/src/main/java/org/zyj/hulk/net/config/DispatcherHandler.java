package org.zyj.hulk.net.config;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyj
 * @date 2022/1/26 19:57
 */
@Slf4j
@Component
public class DispatcherHandler extends ChannelDuplexHandler implements InitializingBean, DisposableBean {
    private ExecutorService businessGroup;

    @PostConstruct
    public void init() {
        this.businessGroup = new ThreadPoolExecutor(200, 200, 300L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory("biz-"));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof String) {
            businessGroup.execute(() -> channelReadInvoke(ctx, (String) msg));
        }
        log.error("DispatcherHandler.channelRead is not String");
        ctx.fireChannelRead(msg);
    }

    @Override
    public void destroy() throws Exception {
        businessGroup.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    private void channelReadInvoke(ChannelHandlerContext ctx, String msg) {

    }
}
