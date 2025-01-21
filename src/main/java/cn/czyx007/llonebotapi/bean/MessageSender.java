package cn.czyx007.llonebotapi.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;

/**
 * 消息发送者数据格式
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class MessageSender {
    /**
     * 发送者 QQ 号
     */
    BigInteger userId;

    /**
     * 昵称
     */
    String nickname;

    /**
     * 性别, male 或 female 或 unknown
     */
    String sex;

    /**
     * 年龄
     */
    Integer age;


    //当私聊类型为群临时会话时的额外字段
    /**
     * 临时群消息来源群号
     */
    BigInteger groupId;


    //群聊时的额外字段
    /**
     * 群名片／备注
     */
    String card;

    /**
     * 地区
     */
    String area;

    /**
     * 成员等级
     */
    String level;

    /**
     * 角色, owner 或 admin 或 member
     */
    String role;

    /**
     * 专属头衔
     */
    String title;
}
