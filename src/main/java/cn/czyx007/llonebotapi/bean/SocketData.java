package cn.czyx007.llonebotapi.bean;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * Websocket消息发送数据格式
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SocketData {
    /**
     * 要执行的动作，详见https://llonebot.apifox.cn/
     */
    String action;

    /**
     * 动作对应的参数
     */
    Map<String, Object> params;
}
