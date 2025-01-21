## LLOneBotApi

### 文档参考：

1、https://llonebot.apifox.cn/

2、https://docs.go-cqhttp.org/event/

3、https://docs.go-cqhttp.org/cqcode/

4、https://docs.go-cqhttp.org/reference/data_struct.html


### 功能描述

基于正向WebSocket(SpringBoot)实现的LLOneBot接口对接，目前已实现以下功能：

1、消息监听(输出到控制台日志)

2、执行禁言(单人或全体)命令，限群主或管理员身份

3、部分群组相关 接口/动作 实现(详见action.GroupAction)：
    
    (1)获取群列表
        
    (2)获取群成员列表
    
    (3)获取群成员信息
    
    (4)设置群名片
    
    (5)禁言某个群成员
    
    (6)全体禁言
    
    (7)发送群聊文本消息
    
    (8)发送群聊回复消息
    
    (9)撤回消息

### 配置文件

配置Websocket链接地址，如：

websocket.url = ws://localhost:3001

websocket.accessToken = 123456 (需与LLOneBot设置的AccessToken相同)

### 开发环境

JDK21、SpringBoot 3.4.1、Lombok 1.18.36、Jackson-databind 2.18.2