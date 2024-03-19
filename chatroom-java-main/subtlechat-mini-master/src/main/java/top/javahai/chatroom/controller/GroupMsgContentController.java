package top.javahai.chatroom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import top.javahai.chatroom.entity.GroupMsgContent;
import top.javahai.chatroom.entity.RespBean;
import top.javahai.chatroom.entity.RespPageBean;
import top.javahai.chatroom.service.GroupMsgContentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * (GroupMsgContent)表控制层
 *
 * @author makejava
 * @since 2020-06-17 10:51:13
 */
@RestController
@RequestMapping("/groupMsgContent")
public class GroupMsgContentController {
    /**
     * 服务对象
     */
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private GroupMsgContentService groupMsgContentService;

    @GetMapping("/")
    private List<GroupMsgContent> getAllGroupMsgContent() {
        return groupMsgContentService.queryAllByLimit(null, null);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public GroupMsgContent selectOne(Integer id) {
        return groupMsgContentService.queryById(id);
    }

    /**
     * 分页返回数据
     *
     * @param page      页数
     * @param size      单页大小
     * @param nickname  发送者昵称
     * @param type      消息类型
     * @param dateScope 发送时间范围
     * @return
     * @author luo
     */
    @GetMapping("/page")
    public RespPageBean getAllGroupMsgContentByPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                    @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                    String nickname, Integer type, Date[] dateScope) {
        return groupMsgContentService.getAllGroupMsgContentByPage(page, size, nickname, type, dateScope);
    }

    /**
     * 根据id删除单条记录
     *
     * @param id
     * @return
     * @author luo
     */
    @DeleteMapping("deleteMessage/{id}")
    public RespBean deleteGroupMsgContentById(@PathVariable Integer id) {
        groupMsgContentService.deleteById(id);
        return RespBean.ok("删除成功！");
    }

    @DeleteMapping("deleteMessages/{ids}")
    public RespBean deleteGroupMsgContentByIds(@PathVariable List<Integer> ids) {
        groupMsgContentService.deleteGroupMsgContentByIds(ids);
        return RespBean.ok("删除成功！");
    }

    //消息删除功能
    @DeleteMapping("/deleteSingleMessage")
    public RespBean deleteMessage(@RequestBody GroupMsgContent groupMsgContent) {
        groupMsgContentService.deleteMessage(groupMsgContent);
        return RespBean.ok("消息删除成功！");
    }

    //消息自毁功能
//    @PostMapping("/selfDestructMessage")
//    public RespBean selfDestructMessage(GroupMsgContent groupMsgContent, @RequestParam(value = "selfDestruct", defaultValue = "false") boolean selfDestruct) {
//        if (selfDestruct) {
//            //如果是自毁消息，设置自毁时间为当前时间+10秒
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.SECOND, 10);
//            groupMsgContent.setSelfDestructTime(calendar.getTime());
//        }
//        //保存消息
//        groupMsgContentService.addSelfDestructMessage(groupMsgContent);
//        // 修改返回信息，包括消息ID和确切的销毁时间
//        return RespBean.ok("此消息将在10秒中后自动删除", new HashMap<String, Object>(){{
//            put("messageId", groupMsgContent.getId());
//            put("destructionTime", groupMsgContent.getSelfDestructTime());
//        }});
//        //return RespBean.ok("此消息将在10秒中后自动删除");
//    }
}