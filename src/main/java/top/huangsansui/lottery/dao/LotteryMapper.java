package top.huangsansui.lottery.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.huangsansui.lottery.dto.LotteryDTO;
import top.huangsansui.lottery.model.Lottery;

import java.util.List;

@Repository
public interface LotteryMapper {
    int deleteByPrimaryKey(Long lotteryId);

    int insert(Lottery record);

    int insertSelective(Lottery record);

    Lottery selectByPrimaryKey(Long lotteryId);

    int updateByPrimaryKeySelective(Lottery record);

    int updateByPrimaryKey(Lottery record);

    List<LotteryDTO> findList();

    Lottery findByLotteryId(@Param("lotteryId")Long lotteryId);

    int decStockNumByLotteryId(@Param("lotteryId")Long lotteryId);
}