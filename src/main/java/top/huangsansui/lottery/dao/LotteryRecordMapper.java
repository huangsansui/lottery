package top.huangsansui.lottery.dao;

import org.springframework.stereotype.Repository;
import top.huangsansui.lottery.model.LotteryRecord;
@Repository
public interface LotteryRecordMapper {
    int deleteByPrimaryKey(Long recordId);

    int insert(LotteryRecord record);

    int insertSelective(LotteryRecord record);

    LotteryRecord selectByPrimaryKey(Long recordId);

    int updateByPrimaryKeySelective(LotteryRecord record);

    int updateByPrimaryKey(LotteryRecord record);
}