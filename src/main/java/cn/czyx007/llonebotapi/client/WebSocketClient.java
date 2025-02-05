package cn.czyx007.llonebotapi.client;

import cn.czyx007.llonebotapi.Utils.CQUtil;
import cn.czyx007.llonebotapi.Utils.PostType;
import cn.czyx007.llonebotapi.action.GroupAction;
import cn.czyx007.llonebotapi.bean.*;
import cn.czyx007.llonebotapi.service.GroupSyncService;
import cn.czyx007.llonebotapi.service.WhiteListService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

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

    @Value("${minecraft.enable}")
    private boolean mcEnable;

    @Value("${api.enable}")
    private boolean apiEnable;

    @Value("${api.chatLimits}")
    private int chatLimits;

    @Value("${api.showThink}")
    private boolean showThink;

    @Value("${api.model}")
    private String model;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${api.key}")
    private String apiKey;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 全局线程池
    private volatile boolean isConnecting = false; // 避免重复连接

    private final GroupSyncService groupSyncService;
    private final WhiteListService whiteListService;

    public WebSocketClient(GroupSyncService groupSyncService, WhiteListService whiteListService) {
        this.groupSyncService = groupSyncService;
        this.whiteListService = whiteListService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        connect();
    }

    private void connect() {
        if (isConnecting) return;

        isConnecting = true;
        try {
            log.info("正在连接到 WebSocket 服务: {}", uri);
            StandardWebSocketClient client = new StandardWebSocketClient();
            if(accessToken != null && !accessToken.isBlank()) {
                WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
                headers.set("Authorization", "Bearer " + accessToken);
                GroupAction.session = client.execute(new MyWebSocketHandler(groupSyncService, whiteListService), headers, URI.create(uri)).get();
            } else {
                GroupAction.session = client.execute(new MyWebSocketHandler(groupSyncService, whiteListService), uri).get();
            }
            isConnecting = false;
        } catch (Exception e) {
            log.error("WebSocket 连接失败: {}", e.getMessage());
            isConnecting = false;
            reconnect();
        }
    }

    private void reconnect() {
        scheduler.schedule(() -> {
            log.info("正在尝试重新连接...");
            connect();
        }, 5, TimeUnit.SECONDS); // 延时 5 秒后重试
    }

    @PreDestroy
    public void cleanup() {
        log.info("清理 WebSocketClient 资源...");
        scheduler.shutdown(); // 应用关闭时释放线程池
    }

    private class MyWebSocketHandler extends TextWebSocketHandler {
        private WebClient webClient;
        private final ObjectMapper objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        private final Set<BigInteger> voteMember = new HashSet<>();
        private final ConcurrentHashMap<BigInteger, CopyOnWriteArrayList<AIMessage>> messages = new ConcurrentHashMap<>();

        private final GroupSyncService groupSyncService;
        private final WhiteListService whiteListService;

        public MyWebSocketHandler(GroupSyncService groupSyncService, WhiteListService whiteListService) {
            this.groupSyncService = groupSyncService;
            this.whiteListService = whiteListService;

            // 初始化WebClient（非阻塞式）
            if (apiEnable) {
                WebClient.Builder builder = WebClient.builder();
                if (apiKey != null && !apiKey.isBlank())
                    builder.defaultHeader("Authorization", "Bearer " + apiKey);
                builder.defaultHeader("Content-Type", "application/json");
                this.webClient = builder.build();
            }
        }

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
                String postType = objectMapper.readTree(payload).path("post_type").asText();
                if (PostType.MESSAGE.equals(postType)) {
                    BotData botData = objectMapper.readValue(payload, BotData.class);
                    //log.info(botData.toString());
                    //群消息
                    if("group".equals(botData.getMessageType())) {
                        String rawMessage = botData.getRawMessage();
                        log.info("用户{}在群{}发送了消息：{}", botData.getUserId(), botData.getGroupId(), rawMessage);
                        if ("#帮助".equals(rawMessage)) {
                            //对应命令0：#帮助
                            String help = "#禁言 @群成员 禁言时长(秒)\n" +
                                    "#禁言 (全体禁言)\n" +
                                    "#取消禁言 @群成员\n" +
                                    "#取消禁言 (全体禁言)\n" +
                                    "#开启消息同步\n" +
                                    "#关闭消息同步";
                            if (mcEnable) {
                                help += "\n#白名单 游戏名\n" +
                                        "#修改白名单 @群成员 游戏名\n" +
                                        "#删除白名单 @群成员\n" +
                                        "#重启投票";
                            }
                            if (apiEnable) {
                                help += "\n#清空聊天记录\n" +
                                        "#+非指令内容可聊天";
                            }
                            GroupAction.sendGroupMsg(botData.getGroupId(), help);
                        } else if (rawMessage.startsWith("#禁言")) {
                            //对应命令1：禁言 @群成员 禁言时长整数值(单位:秒) 或 禁言(全体禁言)
                            //权限要求：群主或管理员
                            if (!"member".equals(botData.getSender().getRole())) {
                                if ("#禁言".equals(rawMessage)) {
                                    GroupAction.setGroupWholeBan(botData.getGroupId(), true);
                                } else {
                                    String[] msg = rawMessage.split(" ");
                                    if (msg.length == 3 && "at".equals(CQUtil.getCQType(msg[1]))) {
                                        try {
                                            Integer duration = Integer.valueOf(msg[2]);
                                            BigInteger userId = CQUtil.getQQ(msg[1]);
                                            GroupAction.setGroupBan(botData.getGroupId(), userId, duration);
                                        } catch (NumberFormatException e) {
                                            GroupAction.sendGroupMsg(botData.getGroupId(), "禁言时长必须为整数(单位:秒)");
                                        }
                                    } else {
                                        GroupAction.sendGroupMsg(botData.getGroupId(), "命令格式错误，应为：#禁言 @群成员 禁言时长整数值(单位:秒)");
                                    }
                                }
                            }
                        } else if (rawMessage.startsWith("#取消禁言")) {
                            //对应命令2：#取消禁言 @群成员 或 #取消禁言(全体禁言)
                            //权限要求：群主或管理员
                            if (!"member".equals(botData.getSender().getRole())) {
                                if ("#取消禁言".equals(rawMessage)) {
                                    GroupAction.setGroupWholeBan(botData.getGroupId(), false);
                                } else {
                                    String[] msg = rawMessage.split(" ");
                                    if (msg.length == 2 && "at".equals(CQUtil.getCQType(msg[1]))) {
                                        BigInteger userId = CQUtil.getQQ(msg[1]);
                                        GroupAction.setGroupBan(botData.getGroupId(), userId, 0);
                                    } else {
                                        GroupAction.sendGroupMsg(botData.getGroupId(), "命令格式错误，应为：#取消禁言 @群成员");
                                    }
                                }
                            }
                        } else if ("#开启消息同步".equals(rawMessage)) {
                            //对应命令3：#开启消息同步，将当前消息来源群加入消息同步列表
                            //权限要求：群主或管理员
                            if (!"member".equals(botData.getSender().getRole())) {
                                //将当前群号加入同步列表
                                if (groupSyncService.addGroup(botData.getGroupId())) {
                                    GroupAction.sendGroupMsg(botData.getGroupId(), "消息同步开启成功！");
                                } else {
                                    GroupAction.sendGroupMsg(botData.getGroupId(), "该群已添加，请勿重复开启！");
                                }
                            }
                        } else if ("#关闭消息同步".equals(rawMessage)) {
                            //对应命令4：#关闭消息同步，将当前消息来源群移出消息同步列表
                            //权限要求：群主或管理员
                            if (!"member".equals(botData.getSender().getRole())) {
                                //将当前群号移出同步列表
                                if (groupSyncService.deleteGroup(botData.getGroupId())) {
                                    GroupAction.sendGroupMsg(botData.getGroupId(), "消息同步关闭成功！");
                                } else {
                                    GroupAction.sendGroupMsg(botData.getGroupId(), "该群已移除，请勿重复关闭！");
                                }
                            }
                        } else if (mcEnable && rawMessage.startsWith("#白名单")) {
                            //对应命令5：绑定白名单
                            //命令格式: #白名单 游戏名
                            String[] msg = rawMessage.split(" ");
                            if (msg.length == 2) {
                                WhiteList wl = whiteListService.getWhiteList(botData.getUserId());
                                if (wl == null) {
                                    String username = rawMessage.split(" ")[1];
                                    if (whiteListService.usernamePatternCheck(username) && !whiteListService.isUsernameExists(username)) {
                                        //用户名格式正确且不存在于数据库
                                        if (whiteListService.addWhiteList(new WhiteList(username, botData.getUserId()))) {
                                            GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "白名单添加成功！");
                                        } else {
                                            GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "白名单添加失败！");
                                        }
                                    } else GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "用户名已存在或格式错误！\n用户名长度需在3到16个字符之间，只能包含字母、数字和下划线，且不能为纯数字");
                                } else GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "你已绑定用户" + wl.getUsername() + "!");
                            } else GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "命令格式错误，应为：#白名单 游戏名");
                        } else if (mcEnable && rawMessage.startsWith("#修改白名单")) {
                            //对应命令6：修改白名单
                            //命令格式: #修改白名单 @群成员 游戏名
                            //权限要求：群主或管理员
                            if (!"member".equals(botData.getSender().getRole())) {
                                String[] msg = rawMessage.split(" ");
                                if (msg.length == 3 && "at".equals(CQUtil.getCQType(msg[1]))) {
                                    String username = msg[2];
                                    if (whiteListService.usernamePatternCheck(username) && !whiteListService.isUsernameExists(username)) {
                                        //用户名格式正确且不存在于数据库
                                        BigInteger userId = CQUtil.getQQ(msg[1]);

                                        boolean success;
                                        WhiteList wl = whiteListService.getWhiteList(userId);
                                        if (wl != null)
                                            success = whiteListService.updateWhiteList(new WhiteList(username, userId), wl.getUsername());
                                        else success = whiteListService.addWhiteList(new WhiteList(username, userId));

                                        if (success)
                                            GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "白名单修改成功！");
                                        else GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "白名单修改失败！");
                                    } else GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "用户名已存在或格式错误！\n用户名长度需在3到16个字符之间，只能包含字母、数字和下划线，且不能为纯数字");
                                } else GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "命令格式错误，应为：#修改白名单 @群成员 游戏名");
                            }
                        } else if (mcEnable && rawMessage.startsWith("#删除白名单")) {
                            //对应命令7：删除白名单
                            //命令格式: #删除白名单 @群成员，需要已存在白名单
                            //权限要求：群主或管理员
                            if (!"member".equals(botData.getSender().getRole())) {
                                String[] msg = rawMessage.split(" ");
                                if (msg.length == 2 && "at".equals(CQUtil.getCQType(msg[1]))) {
                                    BigInteger userId = CQUtil.getQQ(msg[1]);
                                    WhiteList wl = whiteListService.getWhiteList(userId);
                                    if (wl != null) {
                                        if (whiteListService.delWhiteList(wl))
                                            GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "白名单删除成功！");
                                        else GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "白名单删除失败！");
                                    } else {
                                        GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "该用户不存在白名单！");
                                    }
                                } else GroupAction.sendGroupMsgReply(botData.getGroupId(), botData.getMessageId(), "命令格式错误，应为：#删除白名单 @群成员");
                            }
                        } else if (mcEnable && "#重启投票".equals(rawMessage)) {
                            //对应命令8：投票重启服务器，要求投票人数大于等于服务器在线人数一半（向下取整）
                            //命令格式: #重启投票
                            //1.检查投票人是否有白名单且服务器在线，满足条件则尝试投票人数+1
                            WhiteList wl = whiteListService.getWhiteList(botData.getUserId());
                            if (wl != null) {
                                String onlineStatus = whiteListService.isUserOnline(wl.getUsername());
                                if ("true".equals(onlineStatus)) {
                                    voteMember.add(botData.getUserId());
                                    //2.获取服务器在线人数
                                    int currentCount = whiteListService.currentOnlineCount();
                                    log.info("投票人数：{}，服务器在线人数：{}", voteMember.size(), currentCount);
                                    //3.若投票人数大于等于一半则重启，并且清零计数
                                    if (voteMember.size() >= (currentCount / 2)) {
                                        whiteListService.restartServer();
                                        voteMember.clear();
                                        GroupAction.sendGroupMsg(botData.getGroupId(), "投票成功！正在重启服务器");
                                    } else {
                                        GroupAction.sendGroupMsg(botData.getGroupId(),
                                                "当前投票人数：" + voteMember.size() + "/" + currentCount);
                                    }
                                } else if ("false".equals(onlineStatus)) {
                                    GroupAction.sendGroupMsg(botData.getGroupId(), "你不在服务器中！");
                                } else if ("|offline|".equals(onlineStatus)) {
                                    GroupAction.sendGroupMsg(botData.getGroupId(), "服务器未开启！");
                                }
                            } else GroupAction.sendGroupMsg(botData.getGroupId(), "你不在白名单中！");
                        } else if (apiEnable && "#清空聊天记录".equals(rawMessage)) {
                            if (messages.containsKey(botData.getUserId())) {
                                messages.get(botData.getUserId()).clear();
                            }
                            GroupAction.sendGroupMsg(botData.getGroupId(), "聊天记录清空成功！");
                        } else if (apiEnable && rawMessage.startsWith("#")) {
                            callLLMApi(rawMessage.substring(1), botData.getGroupId(), botData.getUserId());
                        } else {
                            //群消息广播
                            if (groupSyncService.ifExists(botData.getGroupId())) {
                                List<GroupSync> list = groupSyncService.getGroupListExcept(botData.getGroupId());
                                if (list != null) {
                                    //1.添加来源头
                                    MessageSender sender = botData.getSender();
                                    //若未设置群名片则群名片为""而不是null
                                    String oriHead = "用户(" +
                                            ("".equals(sender.getCard()) ? sender.getNickname() : sender.getCard()) +
                                            //")在群" + botData.getGroupId() +
                                            ")发送了消息：\n";
                                    //2.拼接消息串
                                    List<Message> messages = botData.getMessage();
                                    Map<String, String> data = new HashMap<>();
                                    data.put("text", oriHead);
                                    messages.addFirst(new Message("text", data));
                                    //3.消息广播
                                    GroupAction.sendMultipleGroupMessages(list, messages);
                                }
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

        // 调用大模型API并异步回复
        private void callLLMApi(String userInput, BigInteger groupId, BigInteger userId) {
            // 初始化或获取线程安全的列表
            CopyOnWriteArrayList<AIMessage> msgList = messages.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
            // 添加 user 消息前检查长度
            synchronized (msgList) { // 对当前 userId 的列表加锁
                if (msgList.size() > chatLimits) {
                    msgList.subList(0, 2).clear(); // 移除最旧的 user 和对应的 assistant
                }
                msgList.add(new AIMessage("user", userInput));
                // 无需显式 put，因 newMsg 是原列表的引用
            }

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.6);
            requestBody.put("messages", objectMapper.convertValue(msgList, List.class));

            // 异步发送请求
            webClient.post()
                    .uri(apiUrl)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError, // 判断是否为错误状态码（如4xx/5xx）
                            response -> {
                                log.error("API 错误状态码: {}", response.statusCode());
                                handleError(msgList);
                                // 返回自定义异常（触发 subscribe 的 onError）
                                return Mono.error(new RuntimeException("API请求失败，状态码: " + response.statusCode()));
                            }
                    )
                    .bodyToMono(String.class)
                    .subscribe(response -> {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(response);
                            String reply = jsonNode.path("choices").get(0)
                                    .path("message").path("content").asText();
                            //解析回复正文（不包含思维链）
                            String newMessage = reply.startsWith("<think>") ? reply.split("</think>\\n\\n")[1] : reply;
                            synchronized (msgList) {
                                msgList.add(new AIMessage("assistant", newMessage));
                            }
                            if (!showThink && model.toLowerCase().contains("deepseek"))
                                reply = newMessage;
                            //若<think>部分为空，直接去除
                            if (reply.startsWith("<think>\n\n</think>\n\n"))
                                reply = reply.replace("<think>\n\n</think>\n\n", "");
                            // 调用原有方法发送群消息
                            GroupAction.sendGroupMsg(groupId, reply);
                        } catch (Exception e) {
                            log.error("API处理失败: {}", e.getMessage());
                            handleError(msgList);
                        }
                    }, error -> {
                        log.error("API调用失败: {}", error.getMessage());
                        handleError(msgList);
                    });
        }

        private void handleError(List<AIMessage> msgList) {
            synchronized (msgList) {
                if (!msgList.isEmpty() && "user".equals(msgList.getLast().getRole())) {
                    msgList.removeLast();
                }
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            log.error("WebSocket 传输错误：{}", exception.getMessage());
            reconnect();
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            log.info("WebSocket 连接已关闭，状态：{}", status);
            reconnect();
        }
    }
}
