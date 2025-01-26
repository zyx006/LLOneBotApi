package cn.czyx007.llonebotapi.action;

import cn.czyx007.llonebotapi.bean.GroupSync;
import cn.czyx007.llonebotapi.bean.Message;
import cn.czyx007.llonebotapi.bean.SocketData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 群组相关接口对接
 */
@Slf4j
@Component
public class GroupAction {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static WebSocketSession session;

    private static final ExecutorService executor = new ThreadPoolExecutor(
            5, // 核心线程数
            10, // 最大线程数
            60L, TimeUnit.SECONDS, // 空闲线程存活时间
            new LinkedBlockingQueue<>(100), // 阻塞队列容量
            Executors.defaultThreadFactory(), // 线程工厂
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用线程运行任务
    );

    /**
     * 发送Websocket数据
     * @param data 待发送的数据
     */
    private static void sendMessage(SocketData data) {
        try {
            //log.info("\nsendMessage:{}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data));
            String jsonString = objectMapper.writeValueAsString(data);
            TextMessage message = new TextMessage(jsonString);
            session.sendMessage(message);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 获取群列表
     */
    public static void getGroupList() {
        Map<String, Object> params = new HashMap<>();
        params.put("no_cache", true);
        sendMessage(new SocketData("get_group_list", params));
    }

    /**
     * 获取群成员列表
     * @param groupId 群号
     */
    public static void getGroupMemberList(BigInteger groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put("no_cache", true);
        params.put("group_id", groupId);
        sendMessage(new SocketData("get_group_member_list", params));
    }

    /**
     * 获取群成员信息
     * @param groupId 群号
     * @param userId 群成员QQ号
     */
    public static void getGroupMemberInfo(BigInteger groupId, BigInteger userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("group_id", groupId);
        sendMessage(new SocketData("get_group_member_info", params));
    }

    /**
     * 设置群名片
     * @param groupId 群号
     * @param userId 群成员QQ号
     * @param card 新名片
     */
    public static void setGroupCard(BigInteger groupId, BigInteger userId, String card) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("card", card);
        sendMessage(new SocketData("set_group_card", params));
    }

    /**
     * 禁言某个群成员
     * @param groupId 群号
     * @param userId 待禁言的群成员QQ号
     * @param duration 禁言时长(秒)，0为取消禁言
     */
    public static void setGroupBan(BigInteger groupId, BigInteger userId, Integer duration) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("duration", duration);
        sendMessage(new SocketData("set_group_ban", params));
    }

    /**
     * 全体禁言
     * @param groupId 群号
     * @param enable true为禁言，false为取消
     */
    public static void setGroupWholeBan(BigInteger groupId, Boolean enable) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("enable", enable);
        sendMessage(new SocketData("set_group_whole_ban", params));
    }

    /**
     * 发送群聊文本消息
     * @param groupId 群号
     * @param messageText 要发送的消息文本字符串
     */
    public static void sendGroupMsg(BigInteger groupId, String messageText) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);

        Map<String, String> data = new HashMap<>();
        data.put("text", messageText);
        List<Message> message = new ArrayList<>();
        message.add(new Message("text", data));
        params.put("message", message);

        sendMessage(new SocketData("send_group_msg", params));
    }

    /**
     * 发送群聊回复消息
     * @param groupId 群号
     * @param messageId 要回复的消息id
     * @param messageText 要发送的消息文本字符串
     */
    public static void sendGroupMsgReply(BigInteger groupId, Integer messageId, String messageText) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);

        List<Message> message = new ArrayList<>();

        Map<String, String> replyData = new HashMap<>();
        replyData.put("id", String.valueOf(messageId));
        message.add(new Message("reply", replyData));

        Map<String, String> data = new HashMap<>();
        data.put("text", messageText);
        message.add(new Message("text", data));

        params.put("message", message);
        sendMessage(new SocketData("send_group_msg", params));
    }

    /**
     * 撤回消息
     * @param messageId 要撤回的消息id
     */
    public static void recallMsg(BigInteger messageId) {
        Map<String, Object> params = new HashMap<>();
        params.put("message_id", messageId);
        sendMessage(new SocketData("delete_msg", params));
    }

    /**
     * 使用现成的消息串发送到群聊（用于消息转发+来源标明）
     * @param groupId 群号
     * @param messages 要发送的消息串
     */
    public static void sendGroupMessage(BigInteger groupId, List<Message> messages) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("message", messages);
        sendMessage(new SocketData("send_group_msg", params));
    }

    /**
     * 并发发送多条消息(用于消息广播同步)，结合sendGroupMessage使用
     * @param groups 要消息同步的群列表
     * @param messages 要发送的消息串
     */
    public static void sendMultipleGroupMessages(List<GroupSync> groups, List<Message> messages) {
        for (GroupSync group : groups) {
            // 提交任务到线程池
            executor.submit(() -> {
                try {
                    sendGroupMessage(group.getGroupId(), messages);
                } catch (Exception e) {
                    log.error("发送消息失败，群组ID: {}，异常信息: {}", group.getGroupId(), e.getMessage(), e);
                }
            });
        }
    }

    /**
     * 关闭全局线程池（如在应用关闭时调用）
     */
    @PreDestroy
    public static void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("线程池未在指定时间内完成任务，强制关闭！");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("线程池关闭时被中断：{}", e.getMessage());
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
