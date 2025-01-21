package cn.czyx007.llonebotapi;

import cn.czyx007.llonebotapi.bean.BotData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LlOneBotApiApplicationTests {

    //自动驼峰映射测试
    @Test
    void contextLoads() throws JsonProcessingException {
        String json = "{\"self_id\":431722705,\"user_id\":1029606625,\"time\":1737451332,\"message_id\":569200371,\"message_seq\":1534,\"message_type\":\"group\",\"sender\":{\"user_id\":1029606625,\"nickname\":\"君绾墨\",\"card\":\"\",\"role\":\"owner\",\"title\":\"\"},\"raw_message\":\"11he\",\"font\":14,\"sub_type\":\"normal\",\"message\":[{\"type\":\"text\",\"data\":{\"text\":\"11he\"}}],\"message_format\":\"array\",\"post_type\":\"message\",\"group_id\":764027101}";
        BotData botData = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).readValue(json, BotData.class);
        System.out.println(botData);
    }

}
