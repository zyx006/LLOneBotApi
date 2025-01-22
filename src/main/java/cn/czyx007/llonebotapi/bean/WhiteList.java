package cn.czyx007.llonebotapi.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;

/**
 * 白名单绑定列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@TableName("whitelist")
public class WhiteList {
    /**
     * 游戏名
     */
    String username;

    /**
     * 用户qq号
     */
    BigInteger id;
}
