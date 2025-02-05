## LLOneBotApi

### 文档参考：

1、https://llonebot.apifox.cn/

2、https://docs.go-cqhttp.org/event/

3、https://docs.go-cqhttp.org/cqcode/

4、https://docs.go-cqhttp.org/reference/data_struct.html


### 功能描述

基于正向WebSocket(SpringBoot)实现的LLOneBot接口对接，支持自动重连和接入AI聊天（默认使用SiliconFlow的DeepSeek-R1 API），目前已实现以下功能：

1、消息监听(输出到控制台日志)

2、执行(取消)禁言(单人或全体)命令，限群主或管理员身份

3、开启(关闭)消息同步，将某个群的消息广播到其他开启消息同步功能的群

4、Minecraft白名单绑定、修改、删除和服务器重启投票功能（基于RCON协议）`可控制启用/禁用`

5、接入AI大模型API，实现群聊AI聊天（仅单轮对话）`可控制启用/禁用`

6、部分群组相关 接口/动作 实现(详见action.GroupAction)：
    
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

1、配置Websocket链接地址(支持自动重连)，如：

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
        # 若使用mysql则需额外配置用户名和密码
        mysql:
          username: root
          password: root
```

注意：使用MySQL作为数据源时需要设置用户名和密码，并且要先手动创建数据库onebot，例如：

```mysql
CREATE database IF NOT EXISTS onebot
CHARACTER SET utf8mb4
COLLATE utf8mb4_bin;
```

3、Minecraft(基于RCON协议)相关配置：

```yaml
minecraft:
    enable: true #控制是否启用该功能
    rcon:
        host: localhost
        port: 23456
        password: rconpwd
```

4、AI聊天相关配置：

可使用任意大模型的API接入，如DeepSeek官方API，SiliconFlow的DeepSeek API，自部署API等

目前**推荐使用SiliconFlow API**，使用该链接注册可获赠2000万Tokens（14元余额）：https://cloud.siliconflow.cn/i/ihKzh4AB

DeepSeek-R1官方配置：

url=https://api.deepseek.com/chat/completions

model=deepseek-reasoner

```yaml
api:
    #控制是否启用该功能
    enable: true 
    #仅对DeepSeek相关模型有效，可控制是否去除回复内容中的<think>部分
    #空<think>块必被去除
    showThink: true
    #以下为SiliconFlow API配置示例
    model: deepseek-ai/DeepSeek-R1
    url: https://api.siliconflow.cn/v1/chat/completions
    key:
```

### 开发环境

JDK21、SpringBoot 3.4.1等