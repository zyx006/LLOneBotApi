package cn.czyx007.llonebotapi.service;

import cn.czyx007.llonebotapi.bean.GroupSync;
import cn.czyx007.llonebotapi.mapper.GroupSyncMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class GroupSyncService extends ServiceImpl<GroupSyncMapper, GroupSync> {
    /**
     * 添加群号到消息同步列表，如果群号已存在则不会添加
     * @param groupId 群号
     * @return true表示不存在且添加成功，false表示已存在或添加失败(如群号为null)
     */
    public boolean addGroup(BigInteger groupId) {
        if (groupId == null) {
            return false;
        }
        if (this.getById(groupId) == null) {
            return this.save(new GroupSync(groupId));
        }
        return false;
    }

    /**
     * 从消息同步列表移除群号
     * @param groupId 群号
     * @return true表示移除成功，false表示不存在或移除失败
     */
    public boolean deleteGroup(BigInteger groupId) {
        if (groupId == null) {
            return false;
        }
        return this.removeById(groupId);
    }

    /**
     * 检查当前群号是否在同步列表
     * @param groupId 群号
     * @return true表示存在，false表示不存在
     */
    public boolean ifExists(BigInteger groupId) {
        return this.getById(groupId) != null;
    }

    /**
     * 返回除该群以外的群列表用于消息广播
     * @param groupId 当前群号
     * @return 群列表，为null表示当前群同步列表为空
     */
    public List<GroupSync> getGroupListExcept(BigInteger groupId) {
        List<GroupSync> list = this.list(new LambdaQueryWrapper<GroupSync>().ne(GroupSync::getGroupId, groupId));
        return !list.isEmpty() ? list : null;
    }
}
