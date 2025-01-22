package cn.czyx007.llonebotapi.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;

/**
 * 要同步群消息的群号列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@TableName("group_sync")
public class GroupSync {
    /**
     * 要同步的群号
     */
    @TableId
    BigInteger groupId;
}
