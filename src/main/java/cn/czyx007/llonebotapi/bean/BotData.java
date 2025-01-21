package cn.czyx007.llonebotapi.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;
import java.util.List;

/**
 * 返回的WebSocket数据格式
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class BotData {
    /**
     * 上报数据类型
     */
    String postType;


    //post_type 为 message 或 message_sent 的上报将会有以下有效通用数据
    /**
     * 消息类型
     * 值限定为private, group
     */
    String messageType;

    /**
     * 表示消息的子类型
     */
    String subType;

    /**
     * 消息 ID
     */
    Integer messageId;

    /**
     * 所在群号
     * 当message_type为group时
     */
    BigInteger groupId;

    /**
     * 发送者 QQ 号
     */
    BigInteger userId;

    /**
     * 一个消息链
     */
    List<Message> message;

    /**
     * CQ 码格式的消息
     */
    String rawMessage;

    /**
     * 字体大小
     */
    Integer font;

    /**
     * 发送者信息
     */
    MessageSender sender;
}
