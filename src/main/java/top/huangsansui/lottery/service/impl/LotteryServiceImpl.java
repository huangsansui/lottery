package top.huangsansui.lottery.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.huangsansui.lottery.dao.LotteryMapper;
import top.huangsansui.lottery.dao.LotteryRecordMapper;
import top.huangsansui.lottery.dao.UserMapper;
import top.huangsansui.lottery.dto.LotteryDTO;
import top.huangsansui.lottery.service.LotteryService;

/**
 * Function:
 *
 * @author: Huangqing
 * @Date: 2019/3/11
 * @since: JDK 1.8
 */
@Service
public class LotteryServiceImpl implements LotteryService {

    @Autowired
    private LotteryMapper lotteryMapper;

    @Autowired
    private LotteryRecordMapper lotteryRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public LotteryDTO lottery(long userId) {

        return null;
    }
}
