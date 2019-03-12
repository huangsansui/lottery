package top.huangsansui.lottery.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.huangsansui.lottery.constant.Constant;
import top.huangsansui.lottery.constant.ResponseEntity;
import top.huangsansui.lottery.dao.LotteryMapper;
import top.huangsansui.lottery.dao.LotteryRecordMapper;
import top.huangsansui.lottery.dao.UserMapper;
import top.huangsansui.lottery.dto.LotteryDTO;
import top.huangsansui.lottery.enums.LotteryTypeEnum;
import top.huangsansui.lottery.factory.SendPrizeProcessor;
import top.huangsansui.lottery.factory.SendPrizeProcessorFactory;
import top.huangsansui.lottery.model.Lottery;
import top.huangsansui.lottery.model.LotteryRecord;
import top.huangsansui.lottery.model.User;
import top.huangsansui.lottery.service.LotteryService;
import top.huangsansui.lottery.util.RedisPoolsUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Function:
 *
 * @author: Huangqing
 * @Date: 2019/3/11
 * @since: JDK 1.8
 */
@Service
@Log4j2
public class LotteryServiceImpl implements LotteryService {

    @Autowired
    private LotteryMapper lotteryMapper;

    @Autowired
    private LotteryRecordMapper lotteryRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SendPrizeProcessorFactory sendPrizeProcessorFactory;

    /**
     * 抽奖，发送奖品并记录
     * 步骤：
     * 1、校验用户以及抽奖合理性
     * 2、获取奖项列表
     * 3、过滤筛选奖项
     * 4、抽奖算法得出奖品
     * 5、减少库存
     * 6、发送奖品
     * 7、记录领取记录
     * @param userId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public ResponseEntity lottery(long userId) {
        if (checkFrequent(userId)) {
            log.error("操作频繁");
            return new ResponseEntity(400, null, "操作过于频繁");
        }
        User user = userMapper.findByUserId(userId);
        if (Objects.isNull(user)) {
            log.error("用户不存在");
            return new ResponseEntity(400, null, "用户不存在");
        }
        if (user.getDrawNum() <= 0) {
            log.error("可抽奖次数不足");
            return new ResponseEntity(400, null, "可抽奖次数不足");
        }
        // 获取奖项列表
        List<LotteryDTO> lotteries = getLotteries();
        // 计算中奖概率概率
        initRatios(lotteries);
        // 过滤掉库存为0的不可能抽到的奖品（除谢谢参与外），同时可以按条件筛选（比如用户同一个奖品只能抽一次）
        List<LotteryDTO> filtered = new ArrayList<>();
        lotteries.stream().forEach(p -> {
            if (p.getType() != LotteryTypeEnum.THANKS.getType() && p.getStockNum() <= 0) {
                return;
            }
            filtered.add(p);
        });
        // 抽奖算法，按照概率获取奖品
        Lottery lottery = getLottery(filtered);
        if (Objects.isNull(lottery)) {
            log.error("系统错误，请重试");
            return new ResponseEntity(400, null, "系统错误，请重试");
        }
        Future task = null;
        try {
            // 减少库存
            reduceStock(lottery);
            // 异步发送奖品
            task = sendPrize(lottery, user);
            // 保存发送记录
            saveRecord(lottery, userId);
            if (!(boolean)task.get()){
                return new ResponseEntity(400, null, "发送礼品异常");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity(400, null, "系统错误");
        }
        return new ResponseEntity(200, lottery, null);
    }

    /**
     * 保存领取记录
     * @param lottery
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void saveRecord(Lottery lottery, long userId) {
        LotteryRecord record = new LotteryRecord();
        record.setLotteryId(lottery.getLotteryId());
        record.setLotteryName(lottery.getLotteryName());
        record.setPoints(lottery.getPoints());
        record.setType(lottery.getType());
        record.setUserId(userId);
        lotteryRecordMapper.insertSelective(record);
    }

    /**
     * 发送奖品
     * @param lottery
     * @param user
     * @return
     */
    @Async("myAsync")
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Future<Boolean> sendPrize(Lottery lottery, User user) {
        SendPrizeProcessor processor = sendPrizeProcessorFactory
                                        .getPrizeProcessor(LotteryTypeEnum.getLotteryTypeEmumByType(lottery.getType()));
        if (processor == null) {
            return new AsyncResult<>(Boolean.FALSE);
        }
        processor.send(user, lottery);
        return new AsyncResult<>(Boolean.TRUE);
    }

    /**
     * 减少库存
     * @param lottery
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void reduceStock(Lottery lottery) {
        if (lotteryMapper.decStockNumByLotteryId(lottery.getLotteryId()) <= 0){
            throw new IllegalStateException("减少库存失败");
        }
    }

    private Lottery getLottery(List<LotteryDTO> filtered) {
        //下面用Stream的collect操作将奖品的list集合转化为TreeMap
        //注意看转化逻辑，TreeMap的每个键值对的key都是前一个TreeMap键值对的key加上奖品本身的中奖概率生
        //成的。(m, l)->{｝被反复调用，直到原stream的元素被消费完毕，其中m对象是不变的，l是stream迭代
        //出来的，每次都是个新元素
        TreeMap<Integer, LotteryDTO> treeMap =
                filtered.stream().collect(TreeMap::new, (m, l) -> {
                    int ratio = 0;
                    if (Objects.nonNull(m) && m.size() > 0) {
                        ratio = m.lastKey();
                    }
                    m.put(ratio + l.getRatio(), l);
                }, TreeMap::putAll);
        // lastKey()： 得到TreeMap的最后一个（最高）键
        // ThreadLocalRandom.current().nextInt(ratioLotteries.lastKey()
        // 从最高键至0的范围内取出一个随机值，不包括最高键。
        LotteryDTO lotteryDTO =
                treeMap.tailMap(ThreadLocalRandom.current()
                        .nextInt(treeMap.lastKey()), false).firstEntry().getValue();
        Lottery lottery = null;
        if ((lottery = checkLottery(lotteryDTO.getLotteryId())) == null) {
            return null;
        }
        return lottery;
    }

    private Lottery checkLottery(Long lotteryId) {
        // 检验奖品合法性以及加锁,防止库存不足
        Lottery lottery = lotteryMapper.findByLotteryId(lotteryId);
        if (lottery == null || lottery.getStockNum() <= 0) {
            return null;
        }
        return lottery;
    }

    private void initRatios(List<LotteryDTO> lotteries) {
        int ratio = Constant.WIN_RATIO;
        // 总库存
        int totalStock = lotteries.stream().mapToInt(p -> p.getStockNum()).sum();
        lotteries.stream().forEach(p -> {
            // 如果是谢谢参与则跳过（谢谢参与 = 1 - 其他奖品概率之和）
            if (p.getType() == LotteryTypeEnum.THANKS.getType()) {
                return;
            }
            // 防止奖品库存全为0
            if (totalStock == 0) {
                p.setRatio(0);
                return;
            }
            // 中奖概率规则： 若有5个商品，库存分别为 1，2，3，4，5
            // 则第一个奖品中奖概率为 50% * ( 1 / (1 + 2 + 3 + 4 + 5) )
            int lotteryRatio = (int) Math.round(new BigDecimal(ratio).multiply(
                    (new BigDecimal(p.getStockNum()).divide(new BigDecimal(totalStock)))).doubleValue());
            p.setRatio(lotteryRatio);
        });
        // 计算谢谢参与概率
        int thanksRatio = 100 - lotteries.stream().mapToInt(p -> p.getRatio()).sum();
        lotteries.stream()
                    .filter(p -> p.getType() == LotteryTypeEnum.THANKS.getType())
                    .findFirst().get().setRatio(thanksRatio);
    }

    private boolean checkFrequent(long userId) {
        String msg = RedisPoolsUtil.get(Constant.USER_LOTTERY + userId);
        if (!StringUtils.isEmpty(msg)) {
            return true;
        }
        // 设置5秒过期的key防止频繁点击
        RedisPoolsUtil.setex(Constant.USER_LOTTERY + userId, System.currentTimeMillis(), 5);
        return false;
    }

    public List<LotteryDTO> getLotteries() {
        List<LotteryDTO> lotteries = lotteryMapper.findList();
        return lotteries;
    }
}
