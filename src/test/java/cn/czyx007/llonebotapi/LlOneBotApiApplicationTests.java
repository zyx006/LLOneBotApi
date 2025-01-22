package cn.czyx007.llonebotapi;

import cn.czyx007.llonebotapi.bean.BotData;
import cn.czyx007.llonebotapi.service.WhiteListService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LlOneBotApiApplicationTests {
    @Autowired
    private WhiteListService whiteListService;

    //自动驼峰映射测试
    @Test
    void contextLoads() throws JsonProcessingException {
        String json = "{\"self_id\":123456,\"user_id\":123456,\"time\":123456,\"message_id\":123456,\"message_seq\":1534,\"message_type\":\"group\",\"sender\":{\"user_id\":123456,\"nickname\":\"abc\",\"card\":\"\",\"role\":\"owner\",\"title\":\"\"},\"raw_message\":\"test\",\"font\":14,\"sub_type\":\"normal\",\"message\":[{\"type\":\"text\",\"data\":{\"text\":\"11he\"}}],\"message_format\":\"array\",\"post_type\":\"message\",\"group_id\":123456}";
        BotData botData = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).readValue(json, BotData.class);
        System.out.println(botData);
    }

}
