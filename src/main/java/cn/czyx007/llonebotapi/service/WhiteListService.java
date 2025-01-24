package cn.czyx007.llonebotapi.service;

import cn.czyx007.llonebotapi.bean.WhiteList;
import cn.czyx007.llonebotapi.mapper.WhiteListMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.glavo.rcon.AuthenticationException;
import org.glavo.rcon.Rcon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;

@Slf4j
@Service
public class WhiteListService extends ServiceImpl<WhiteListMapper, WhiteList> {
    @Value("${minecraft.rcon.host}")
    private String host;

    @Value("${minecraft.rcon.port}")
    private int port;

    @Value("${minecraft.rcon.password}")
    private String password;

    private static int currentCount = 0;
    private static String onlineUsers = "";

    /**
     * rcon白名单命令封装
     * @param command 要执行的命令前缀(如whitelist add )
     * @param type 要执行的命令类型，用于日志
     * @param username 要操作的用户名
     * @return true表示rcon执行成功，false表示执行失败
     */
    private boolean rconCommand(String command, String type, String username) {
        if (type.contains("白名单"))
            log.info("开始为玩家 [{}] {}", username, type);
        try (Rcon rcon = new Rcon(host, port, password)) {
            log.info("已成功连接到服务器 {}:{}", host, port);
            try {
                log.info("发送{}命令", type);
                String res = rcon.command(command + username);
                if (res.startsWith("There are ")){
                    currentCount = Integer.parseInt(res.split(" ")[2].split("/")[0]);
                    if (currentCount > 0)
                        onlineUsers = res.split(":")[1];
                }
                log.info("{}命令执行成功", type);
                return true;
            } catch (IOException e) {
                log.error("执行{}命令失败: {}", type, e.getMessage());
            }
        } catch (IOException | AuthenticationException e) {
            log.error("连接到RCON服务器失败 ({}:{}): {}", host, port, e.getMessage());
            if ("list".equals(command)) {
                currentCount = 0;
                onlineUsers = "|offline|";
            }
        }
        return false;
    }

    /**
     * 通过Rcon添加白名单
     * @param whiteList 包含游戏名，qq号的whitelist对象
     * @return true表示rcon命令执行成功且数据库保存成功，false表示白名单添加失败
     */
    public boolean addWhiteList(WhiteList whiteList) {
        if (rconCommand("whitelist add ", "添加白名单", whiteList.getUsername()))
            return this.save(whiteList);
        else return false;
    }

    /**
     * 通过rcon删除白名单
     * @param whiteList 包含游戏名，qq号的whitelist对象
     * @return true表示删除成功，false表示删除失败
     */
    public boolean delWhiteList(WhiteList whiteList) {
        if(rconCommand("whitelist remove ", "删除白名单" , whiteList.getUsername()))
            return this.removeById(whiteList.getId());
        else return false;
    }

    /**
     * 通过rcon更新白名单
     * @param whiteList 包含新游戏名，qq号的whitelist对象
     * @param previousUsername 更新之前的游戏名
     * @return true表示更新成功，false表示更新失败
     */
    public boolean updateWhiteList(WhiteList whiteList, String previousUsername) {
        if (rconCommand("whitelist remove ", "删除白名单" , previousUsername))
            if (rconCommand("whitelist add ", "添加白名单" , whiteList.getUsername()))
                return this.updateById(whiteList);
        return false;
    }

    /**
     * 根据QQ号查询对应白名单
     * @param id QQ号
     * @return 存在则返回白名单对象，不存在返回null
     */
    public WhiteList getWhiteList(BigInteger id) {
        return this.getById(id);
    }

    /**
     * 查询用户名是否已存在
     * @param username 查询的用户名
     * @return true表示已存在，false表示不存在
     */
    public boolean isUsernameExists(String username) {
        return this.getOne(new LambdaQueryWrapper<WhiteList>().eq(WhiteList::getUsername, username)) != null;
    }

    /**
     * 用户名校验，判断格式是否正确
     * 用户名长度需在3到16个字符之间，只能包含字母、数字和下划线，且不能为纯数字
     * @param username 待校验的用户名
     * @return true表示校验通过
     */
    public boolean usernamePatternCheck(String username) {
        return username.matches("^(?!\\d+$)[a-zA-Z0-9_]{3,16}$");
    }

    /**
     * 获取当前在线人数
     * @return 当前在线人数
     */
    public int currentOnlineCount() {
        rconCommand("list", "在线人数获取", "");
        return currentCount;
    }

    /**
     * 检查当前用户名是否服务器在线
     * @param username 用户名
     * @return "true"表示在线，"|offline|"表示服务器未开启或RCON连接失败
     */
    public String isUserOnline(String username) {
        rconCommand("list", "在线玩家列表获取", "");
        return "|offline|".equals(onlineUsers) ? "|offline|" : (onlineUsers.contains(username) ? "true" : "false");
    }

    /**
     * 重启服务器
     * @return true表示重启成功
     */
    public boolean restartServer() {
        return rconCommand("restart", "重启", "");
    }
}
