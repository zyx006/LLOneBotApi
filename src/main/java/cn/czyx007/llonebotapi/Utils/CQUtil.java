package cn.czyx007.llonebotapi.Utils;

import java.math.BigInteger;

/**
 * CQ码解析工具
 * [CQ:类型,参数=值,参数=值]
 */
public class CQUtil {
    /**
     * 获取CQ码类型
     * @param CQString CQ码字符串
     * @return CQ码类型
     */
    public static String getCQType(String CQString) {
        return CQString.split(",")[0].split(":")[1];
    }

    /**
     * 用于获取CQ码第二部分(第一个参数为qq时)的值
     * @param CQString CQ码字符串
     * @return QQ号
     */
    public static BigInteger getQQ(String CQString) {
        return new BigInteger(CQString.split(",")[1].split("=")[1]);
    }
}
