package top.huangsansui.lottery.constant;

/**
 * Function:
 *
 * @author: Huangqing
 * @Date: 2019/3/12
 * @since: JDK 1.8
 */
public class ResponseEntity<T> {

    private int code;

    private T data;

    private String msg;

    public ResponseEntity(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
