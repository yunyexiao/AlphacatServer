package com.alphacat.service;

import com.alphacat.vo.*;

import java.util.List;

public interface TaskService {

    List<IdleTaskVO> getIdle(int requesterId);

    List<UnderwayTaskVO> getUnderway(int requesterId);

    List<EndedTaskVO> getEnded(int requesterId);

    List<AvailableTaskVO> getAvailable(int workerId);

    List<HistoryTaskVO> getHistory(int workerId);

    /**
     * 发起者修改已有任务时获取任务对象
     * @param taskId 任务ID
     * @return 返回的任务对象
     */
    TaskVO get(int taskId);

    /**
     * Allocate a new task id and add this task, along with its labels.
     * @return task id
     * @throws  NullPointerException new id allocation failed
     */
    int add(TaskVO taskVO);

    void update(TaskVO taskVO);

    void delete(int taskId);
}
