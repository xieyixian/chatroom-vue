package top.javahai.chatroom.service.impl;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.javahai.chatroom.entity.GroupMsgContent;
import top.javahai.chatroom.dao.GroupMsgContentDao;
import top.javahai.chatroom.entity.RespPageBean;
import top.javahai.chatroom.entity.User;
import top.javahai.chatroom.service.GroupMsgContentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * (GroupMsgContent)表服务实现类
 *
 * @author makejava
 * @since 2020-06-17 10:51:13
 */
@Service("groupMsgContentService")
public class GroupMsgContentServiceImpl implements GroupMsgContentService {
    @Resource
    private GroupMsgContentDao groupMsgContentDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public GroupMsgContent queryById(Integer id) {
        return groupMsgContentDao.queryById(id);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<GroupMsgContent> queryAllByLimit(Integer offset, Integer limit) {
        return groupMsgContentDao.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param groupMsgContent 实例对象
     * @return 实例对象
     */
    @Override
    public GroupMsgContent insert(GroupMsgContent groupMsgContent) {
        groupMsgContentDao.insert(groupMsgContent);
        return groupMsgContent;
    }

    /**
     * 修改数据
     *
     * @param groupMsgContent 实例对象
     * @return 实例对象
     */
    @Override
    public GroupMsgContent update(GroupMsgContent groupMsgContent) {
        groupMsgContentDao.update(groupMsgContent);
        return this.queryById(groupMsgContent.getId());
    }

    @Override
    public RespPageBean getAllGroupMsgContentByPage(Integer page, Integer size, String nickname, Integer type, Date[] dateScope) {
        if (page != null && size != null) {
            page = (page - 1) * size;
        }
        List<GroupMsgContent> allGroupMsgContentByPage = groupMsgContentDao.getAllGroupMsgContentByPage(page, size, nickname, type, dateScope);
        Long total = groupMsgContentDao.getTotal(nickname, type, dateScope);
        RespPageBean respPageBean = new RespPageBean();
        respPageBean.setData(allGroupMsgContentByPage);
        respPageBean.setTotal(total);
        return respPageBean;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public void deleteById(Integer id) {
        groupMsgContentDao.deleteById(id);
    }

    @Override
    public void deleteGroupMsgContentByIds(List<Integer> ids) {
        groupMsgContentDao.deleteGroupMsgContentByIds(ids);
    }

    @Override
    public void deleteMessage(GroupMsgContent groupMsgContent) {
        //直接获取当前线程的HttpServletRequest对象
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        //从请求中获取 "loggedInUser" 属性
        User user = (User) req.getSession().getAttribute("loggedInUser");
        //验证 "loggedInUser" 是否为null
        if (user == null) {
            throw new IllegalStateException("用户未登录，无法执行删除操作");
        }
        Integer userId = user.getId();
        Integer fromId = groupMsgContent.getFromId();
        //检查当前用户是否有权限删除消息
        if (userId.equals(fromId)) {
            groupMsgContentDao.deleteMessage(groupMsgContent.getId());
        } else {
            throw new IllegalStateException("此用户无权限撤回他人消息！");
        }
    }

//    @Override
//    public void addSelfDestructMessage(GroupMsgContent groupMsgContent) {
//        groupMsgContent.setCreateTime(new Date());
//        groupMsgContentDao.addSelfDestructMessage(groupMsgContent);
//        //如果是自毁消息，设置定时任务删除消息
//        if (groupMsgContent.getSelfDestructTime() != null) {
//            TimerTask task = new TimerTask() {
//                @Override
//                public void run() {
//                    //删除消息
//                    deleteById(groupMsgContent.getId());
//                }
//            };
//            //计算延迟时间
//            long delay = groupMsgContent.getSelfDestructTime().getTime() - System.currentTimeMillis();
//            //创建定时器
//            Timer timer = new Timer();
//            timer.schedule(task, delay);
//        }
//    }
}