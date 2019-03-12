package top.huangsansui.lottery.enums;

/**
 * Function:
 *
 * @author: Huangqing
 * @Date: 2019/3/12
 * @since: JDK 1.8
 */
public enum LotteryTypeEnum {

    THANKS(0, "谢谢参与", "sendThanksProcessor"),

    POINTS(1, "积分", "sendPointsProcessor"),

    MONEY(2, "红包", "sendMoneyProcessor");

    private int type;

    private String msg;

    private String beanName;

    public static LotteryTypeEnum getLotteryTypeEmumByType(int type) {
        for (LotteryTypeEnum lotteryTypeEnum : LotteryTypeEnum.values()) {
            if (lotteryTypeEnum.getType() == type) {
                return lotteryTypeEnum;
            }
        }
        return null;
    }

    LotteryTypeEnum(int type, String msg, String beanName) {
        this.type = type;
        this.msg = msg;
        this.beanName = beanName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
