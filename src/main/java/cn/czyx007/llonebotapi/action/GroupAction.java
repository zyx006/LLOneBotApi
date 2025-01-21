package cn.czyx007.llonebotapi.action;

import cn.czyx007.llonebotapi.bean.Message;
import cn.czyx007.llonebotapi.bean.SocketData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组相关接口对接
 */
@Slf4j
public class GroupAction {
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送Websocket数据
     * @param session Websocket Session
     * @param data 待发送的数据
     */
    private static void sendMessage(WebSocketSession session, SocketData data) {
        try {
            log.info("sendMessage:{}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data));

            String jsonString = objectMapper.writeValueAsString(data);
            TextMessage message = new TextMessage(jsonString);
            session.sendMessage(message);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 获取群列表
     * @param session Websocket Session
     */
    public static void getGroupList(WebSocketSession session) {
        Map<String, Object> params = new HashMap<>();
        params.put("no_cache", true);
        sendMessage(session, new SocketData("get_group_list", params));
    }

    /**
     * 获取群成员列表
     * @param session Websocket Session
     * @param groupId 群号
     */
    public static void getGroupMemberList(WebSocketSession session, BigInteger groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put("no_cache", true);
        params.put("group_id", groupId);
        sendMessage(session, new SocketData("get_group_member_list", params));
    }

    /**
     * 获取群成员信息
     * @param session Websocket Session
     * @param groupId 群号
     * @param userId 群成员QQ号
     */
    public static void getGroupMemberInfo(WebSocketSession session, BigInteger groupId, BigInteger userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("group_id", groupId);
        sendMessage(session, new SocketData("get_group_member_info", params));
    }

    /**
     * 设置群名片
     * @param session Websocket Session
     * @param groupId 群号
     * @param userId 群成员QQ号
     * @param card 新名片
     */
    public static void setGroupCard(WebSocketSession session, BigInteger groupId, BigInteger userId, String card) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("card", card);
        sendMessage(session, new SocketData("set_group_card", params));
    }

    /**
     * 禁言某个群成员
     * @param session Websocket Session
     * @param groupId 群号
     * @param userId 待禁言的群成员QQ号
     * @param duration 禁言时长(秒)，0为取消禁言
     */
    public static void setGroupBan(WebSocketSession session, BigInteger groupId, BigInteger userId, Integer duration) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("duration", duration);
        sendMessage(session, new SocketData("set_group_ban", params));
    }

    /**
     * 全体禁言
     * @param session Websocket Session
     * @param groupId 群号
     * @param enable true为禁言，false为取消
     */
    public static void setGroupWholeBan(WebSocketSession session, BigInteger groupId, Boolean enable) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("enable", enable);
        sendMessage(session, new SocketData("set_group_whole_ban", params));
    }

    /**
     * 发送群聊文本消息
     * @param session Websocket Session
     * @param groupId 群号
     * @param messageText 要发送的消息文本字符串
     */
    public static void sendGroupMsg(WebSocketSession session, BigInteger groupId, String messageText) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);

        Map<String, String> data = new HashMap<>();
        data.put("text", messageText);
        List<Message> message = new ArrayList<>();
        message.add(new Message("text", data));
        params.put("message", message);

        sendMessage(session, new SocketData("send_group_msg", params));
    }

    /**
     * 发送群聊回复消息
     * @param session Websocket Session
     * @param groupId 群号
     * @param messageId 要回复的消息id
     * @param messageText 要发送的消息文本字符串
     */
    public static void sendGroupMsgReply(WebSocketSession session, BigInteger groupId, BigInteger messageId, String messageText) {
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
        sendMessage(session, new SocketData("send_group_msg", params));
    }

    /**
     * 撤回消息
     * @param session Websocket Session
     * @param messageId 要撤回的消息id
     */
    public static void recallMsg(WebSocketSession session, BigInteger messageId) {
        Map<String, Object> params = new HashMap<>();
        params.put("message_id", messageId);
        sendMessage(session, new SocketData("delete_msg", params));
    }
}
