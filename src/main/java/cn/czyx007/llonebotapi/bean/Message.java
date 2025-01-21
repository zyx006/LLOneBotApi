package cn.czyx007.llonebotapi.bean;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * 数组格式的消息串数据格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message {
    /**
     * 消息类型，详见https://docs.go-cqhttp.org/cqcode
     * 常用：text, reply, at
     */
    String type;

    /**
     * 数据参数
     */
    Map<String, String> data;
}
