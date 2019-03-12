package top.huangsansui.lottery.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.huangsansui.lottery.dao.UserMapper;
import top.huangsansui.lottery.model.Lottery;
import top.huangsansui.lottery.model.User;

/**
 * Function:
 *
 * @author Huangqing
 * @date 2019/3/12 11:08
 * @since JKD 1.8
 */
public class SendMoneyProcessor implements SendPrizeProcessor {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void send(User user, Lottery lottery) {
        user.setMoney(user.getMoney() + lottery.getPoints());
        userMapper.updateByPrimaryKeySelective(user);
    }
}
