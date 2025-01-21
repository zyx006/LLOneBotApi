package cn.czyx007.llonebotapi.Utils;


/**
 * 上报数据类型
 * 注: message与message_sent的数据是一致的, 区别仅在于后者是bot发出的消息.
 * 默认配置下不会上报message_sent, 仅在配置message下report-self-message项为true时上报
 */
public class PostType {
    /**
     * 消息
     */
    public static final String MESSAGE = "message";

    /**
     * 消息发送
     */
    public static final String MESSAGE_SENT = "message_sent";

    /**
     * 请求
     */
    public static final String REQUEST = "request";

    /**
     * 通知
     */
    public static final String NOTICE = "notice";

    /**
     * 元事件
     */
    public static final String META_EVENT = "meta_event";
}
