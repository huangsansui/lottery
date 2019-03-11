package top.huangsansui.lottery.enums;

/**
 * Function:
 *
 * @author: Huangqing
 * @Date: 2019/3/12
 * @since: JDK 1.8
 */
public enum LotteryTypeEnum {

    THANKS(0, "谢谢参与"),

    POINTS(1, "积分"),

    MONEY(2, "红包");

    private int type;

    private String msg;

    LotteryTypeEnum(int type, String msg) {
        this.type = type;
        this.msg = msg;
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
}
