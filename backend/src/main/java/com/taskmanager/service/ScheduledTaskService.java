package com.taskmanager.service;

import com.taskmanager.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private MemberLoadService memberLoadService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyCheck() {
        logger.info("开始执行每日任务检查...");
        
        taskService.checkDelayedTasks();
        logger.info("延期任务检查完成");
        
        List<Task> upcomingTasks = taskService.getUpcomingDeadlineTasks(3);
        logger.info("发现 {} 个即将在3天内到期的任务", upcomingTasks.size());
        for (Task task : upcomingTasks) {
            logger.warn("任务即将到期: {} - {} - 截止日期: {}", 
                task.getId(), task.getName(), task.getPlannedEndDate());
        }
        
        List<String> overloadedMembers = memberLoadService.getOverloadedMembers();
        if (!overloadedMembers.isEmpty()) {
            logger.warn("发现负载过高的成员: {}", overloadedMembers);
        }
        
        logger.info("每日任务检查完成");
    }
}
