## LLOneBotApi

### 文档参考：

1、https://llonebot.apifox.cn/

2、https://docs.go-cqhttp.org/event/

3、https://docs.go-cqhttp.org/cqcode/

4、https://docs.go-cqhttp.org/reference/data_struct.html


### 功能描述

基于正向WebSocket(SpringBoot)实现的LLOneBot接口对接，目前已实现以下功能：

1、消息监听(输出到控制台日志)

2、执行(取消)禁言(单人或全体)命令，限群主或管理员身份

3、开启(关闭)消息同步，将某个群的消息广播到其他开启消息同步功能的群

4、Minecraft白名单绑定、修改、删除功能（基于RCON协议）

5、部分群组相关 接口/动作 实现(详见action.GroupAction)：
    
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

1、配置Websocket链接地址，如：

```yaml
websocket:
    url: ws://localhost:3001
    accessToken: 123456 #(需与LLOneBot设置的AccessToken相同)
```

2、指定数据源配置，如：

```yaml
spring:
    datasource:
        # 选择使用哪个数据源配置
        active-db: sqlite  # 可选值: sqlite, mysql
```

注意：使用MySQL作为数据源时需要设置用户名和密码，并且要先手动创建数据库onebot，例如：

```mysql
CREATE database IF NOT EXISTS onebot
CHARACTER SET utf8mb4
COLLATE utf8mb4_bin;
```

3、Minecraft白名单(基于RCON协议)相关配置：

```yaml
minecraft:
    rcon:
        host: localhost
        port: 23456
        password: rconpwd
```

### 开发环境

JDK21、SpringBoot 3.4.1等