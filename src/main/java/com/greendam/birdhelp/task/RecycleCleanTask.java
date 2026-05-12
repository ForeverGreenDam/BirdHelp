package com.greendam.birdhelp.task;

import com.greendam.birdhelp.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 回收站定时清理任务，每日凌晨 3 点执行。
 */
@Slf4j
@Component
public class RecycleCleanTask {

    @Resource
    private FileService fileService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredRecycleFiles() {
        log.info("开始执行回收站过期文件清理");
        int count = fileService.cleanExpiredRecycle();
        log.info("回收站过期文件清理完成，共清理 {} 个文件", count);
    }
}
