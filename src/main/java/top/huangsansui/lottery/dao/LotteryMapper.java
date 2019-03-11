package top.huangsansui.lottery.dao;

import org.springframework.stereotype.Repository;
import top.huangsansui.lottery.model.Lottery;
@Repository
public interface LotteryMapper {
    int deleteByPrimaryKey(Long lotteryId);

    int insert(Lottery record);

    int insertSelective(Lottery record);

    Lottery selectByPrimaryKey(Long lotteryId);

    int updateByPrimaryKeySelective(Lottery record);

    int updateByPrimaryKey(Lottery record);
}