package top.huangsansui.lottery.factory;

import io.netty.util.internal.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import top.huangsansui.lottery.enums.LotteryTypeEnum;

/**
 * Function:
 *
 * @author Huangqing
 * @date 2019/3/12 10:57
 * @since JKD 1.8
 */
@Component
@Log4j2
public class SendPrizeProcessorFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public SendPrizeProcessor getPrizeProcessor(LotteryTypeEnum lotteryTypeEnum) {
        String processorName = lotteryTypeEnum.getBeanName();
        if (processorName == null) {
            return null;
        }
        SendPrizeProcessor sendPrizeProcessor = applicationContext.getBean(processorName, SendPrizeProcessor.class);
        if (sendPrizeProcessor == null) {
            log.error("未找到名称为 【" + processorName +"】的发送奖品器");
            return null;
        }
        return sendPrizeProcessor;
    }

}
