package cn.czyx007.llonebotapi.client;

import cn.czyx007.llonebotapi.Utils.PostType;
import cn.czyx007.llonebotapi.action.GroupAction;
import cn.czyx007.llonebotapi.bean.BotData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.math.BigInteger;
import java.net.URI;

/**
 * Websocket客户端，用于Websocket链接的建立，信息处理等
 */
@Component
@Slf4j
public class WebSocketClient {
    @Value("${websocket.url}")
    private String uri;

    @Value("${websocket.accessToken}")
    private String accessToken;

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            if(accessToken != null && !accessToken.isBlank()) {
                WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
                headers.set("Authorization", "Bearer " + accessToken);
                client.execute(new MyWebSocketHandler(), headers, URI.create(uri));
            } else {
                client.execute(new MyWebSocketHandler(), uri);
            }
            log.info("正在连接到 WebSocket 服务: {}", uri);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private static class MyWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            log.info("连接到 WebSocket 服务成功！");
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            try {
                //负载获取
                String payload = message.getPayload();
                //解析数据类型
                String postType = (String) new JSONParser(payload).parseObject().get("post_type");
                if (PostType.MESSAGE.equals(postType)) {
                    BotData botData = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).readValue(payload, BotData.class);
                    log.info(botData.toString());
                    String rawMessage = botData.getRawMessage();
                    log.info("用户{}在群{}发送了消息：{}", botData.getUserId(), botData.getGroupId(), rawMessage);
                    //对应命令1：禁言 @xxx duration(禁言某人duration秒) 或 禁言(全体禁言)
                    if (rawMessage.startsWith("禁言")) {
                        //权限要求：群主或管理员
                        if (!"member".equals(botData.getSender().getRole())) {
                            if("禁言".equals(rawMessage)) {
                                GroupAction.setGroupWholeBan(session, botData.getGroupId(), true);
                            } else {
                                String[] msg = rawMessage.split(" ");
                                BigInteger userId = BigInteger.valueOf(Long.parseLong(msg[1].split(",")[1].split("=")[1]));
                                GroupAction.setGroupBan(session, botData.getGroupId(), userId, Integer.valueOf(msg[2]));
                            }
                        }
                    }
                } else if (!PostType.META_EVENT.equals(postType)) {
                    //不记录元事件
                    log.info("收到 WebSocket 消息：{}", payload);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            log.error("WebSocket 传输错误：{}", exception.getMessage());
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            log.info("WebSocket 连接已关闭，状态：{}", status);
        }
    }
}
