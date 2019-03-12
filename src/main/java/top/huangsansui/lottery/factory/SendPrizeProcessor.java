package top.huangsansui.lottery.factory;

import top.huangsansui.lottery.model.Lottery;
import top.huangsansui.lottery.model.User;

/**
 * Function:
 *
 * @author Huangqing
 * @date 2019/3/12 11:02
 * @since JKD 1.8
 */
public interface SendPrizeProcessor {

    void send(User user, Lottery lottery);
}
