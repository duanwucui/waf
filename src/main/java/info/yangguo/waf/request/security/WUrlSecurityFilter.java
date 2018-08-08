package info.yangguo.waf.request.security;

import com.codahale.metrics.Timer;
import info.yangguo.waf.Constant;
import info.yangguo.waf.WafHttpHeaderNames;
import info.yangguo.waf.model.SecurityConfigIterm;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author:杨果
 * @date:2017/5/15 上午9:27
 * <p>
 * Description:
 */
public class WUrlSecurityFilter extends SecurityFilter {
    private static final Logger logger = LoggerFactory.getLogger(WUrlSecurityFilter.class);

    @Override
    public boolean isBlacklist() {
        return false;
    }

    @Override
    public boolean doFilter(HttpRequest originalRequest, HttpObject httpObject, ChannelHandlerContext channelHandlerContext, List<SecurityConfigIterm> iterms) {
        if (httpObject instanceof HttpRequest) {
            logger.debug("filter:{}", this.getClass().getName());
            HttpRequest httpRequest = (HttpRequest) httpObject;
            String url;
            int index = httpRequest.uri().indexOf("?");
            if (index > -1) {
                url = httpRequest.uri().substring(0, index);
            } else {
                url = httpRequest.uri();
            }
            for (SecurityConfigIterm iterm : iterms) {
                if (iterm.getConfig().getIsStart()) {
                    Timer itermTimer = Constant.metrics.timer("WUrlSecurityFilter[" + iterm.getName() + "]");
                    Timer.Context itermContext = itermTimer.time();
                    try {
                        Pattern pattern = Pattern.compile(iterm.getName());
                        Matcher matcher = pattern.matcher(url);
                        if (matcher.find()) {
                            hackLog(logger, originalRequest.headers().getAsString(WafHttpHeaderNames.X_REAL_IP), "WUrl", iterm.getName());
                            return true;
                        }
                    } finally {
                        itermContext.stop();
                    }
                }
            }
        }
        return false;
    }
}