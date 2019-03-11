package top.huangsansui.lottery.service;

import top.huangsansui.lottery.dto.LotteryDTO;

/**
 * Function:
 *
 * @author: Huangqing
 * @Date: 2019/3/11
 * @since: JDK 1.8
 */
public interface LotteryService {

    /**
     * 抽奖方法，并发送奖品以及保存中奖记录
     * @param userId
     * @return
     */
    LotteryDTO lottery(long userId);
}
