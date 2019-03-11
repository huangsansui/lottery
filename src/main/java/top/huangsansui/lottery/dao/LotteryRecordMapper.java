package top.huangsansui.lottery.dao;

import top.huangsansui.lottery.model.LotteryRecord;

public interface LotteryRecordMapper {
    int deleteByPrimaryKey(Long recordId);

    int insert(LotteryRecord record);

    int insertSelective(LotteryRecord record);

    LotteryRecord selectByPrimaryKey(Long recordId);

    int updateByPrimaryKeySelective(LotteryRecord record);

    int updateByPrimaryKey(LotteryRecord record);
}