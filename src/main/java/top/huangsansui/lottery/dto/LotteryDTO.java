package top.huangsansui.lottery.dto;

import lombok.Data;

/**
 * Function:
 *
 * @author: Huangqing
 * @Date: 2019/3/11
 * @since: JDK 1.8
 */
@Data
public class LotteryDTO {

    // 奖品id

    private Long lotteryId;

    // 奖品名称

    private String lotteryName;

    // 类型（0=谢谢参与，1=积分，2=红包）

    private Integer type;

    // 奖品积分/红包金额

    private Integer points;

    // 库存量

    private Integer stockNum;

    // 概率

    private Integer ratio;
}
