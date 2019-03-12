package top.huangsansui.lottery.factory;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.huangsansui.lottery.model.Lottery;
import top.huangsansui.lottery.model.User;

/**
 * Function:
 *
 * @author Huangqing
 * @date 2019/3/12 11:08
 * @since JKD 1.8
 */
public class SendThanksProcessor implements SendPrizeProcessor {


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void send(User user, Lottery lottery) {
        return;
    }
}
