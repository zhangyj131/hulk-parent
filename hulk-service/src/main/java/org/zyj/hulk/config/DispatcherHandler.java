package org.zyj.hulk.config;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * @author zhangyj
 * @date 2022/1/26 19:57
 */
@Slf4j
@Component
public class DispatcherHandler extends ChannelDuplexHandler implements InitializingBean, DisposableBean {
    private ExecutorService executorService;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof String) {
            executorService.execute(() -> channelReadInvoke(ctx, (String) msg));
        }
        log.error("DispatcherHandler.channelRead is not String");
        ctx.fireChannelRead(msg);
    }

    @Override
    public void destroy() throws Exception {
        executorService.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    private void channelReadInvoke(ChannelHandlerContext ctx, String msg) {

    }
}
