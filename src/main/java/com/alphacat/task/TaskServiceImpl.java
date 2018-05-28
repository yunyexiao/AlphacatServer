package com.alphacat.task;

import com.alphacat.mapper.LabelMapper;
import com.alphacat.mapper.TaskMapper;
import com.alphacat.pojo.*;
import com.alphacat.service.TaskService;
import com.alphacat.task.schedule.TaskEndScheduler;
import com.alphacat.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskConverter taskConverter;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private LabelMapper labelMapper;
    @Autowired
    private TaskEndScheduler scheduler;

    @Override
    public List<IdleTaskVO> getIdle(int requesterId) {
        List<IdleTask> tasks = taskMapper.getIdleTasks(requesterId);
        return taskConverter.toIdleVOList(tasks);
    }

    @Override
    public List<UnderwayTaskVO> getUnderway(int requesterId) {
        List<UnderwayTask> tasks = taskMapper.getUnderwayTasks(requesterId);
        return taskConverter.toUnderwayVOList(tasks);
    }

    @Override
    public List<EndedTaskVO> getEnded(int requesterId) {
        List<EndedTask> tasks = taskMapper.getEndedTask(requesterId);
        return taskConverter.toEndedVOList(tasks);
    }

    @Override
    public TaskVO get(int id) {
        return taskConverter.toVO(taskMapper.get(id));
    }

    @Override
    public List<AvailableTaskVO> getAvailable(int workerId) {
        List<AvailableTask> tasks = taskMapper.getAvailableTask(workerId);
        return taskConverter.toAvailableVOList(tasks);
    }

    @Override
    public List<AvailableTaskVO> getPartaking(int workerId) {
        List<AvailableTask> tasks = taskMapper.getPartakingTask(workerId);
        return taskConverter.toAvailableVOList(tasks);
    }

    @Override
    public List<HistoryTaskVO> getHistory(int workerId) {
        List<HistoryTask> tasks = taskMapper.getHistoryTasks(workerId);
        return taskConverter.toHistoryVOList(tasks);
    }

    @Override
    public int add(TaskVO taskVO) {
        Integer id = taskMapper.getNewId();
        if(id == null) {
            throw new NullPointerException("Cannot allocate new id for a task.");
        }
        taskVO.setId(id);
        // add task's basic info and settings
        Task task = taskConverter.toPOJO(taskVO);
        taskMapper.add(task);
        // add labels
        taskVO.getLabels().forEach(l -> labelMapper.add(taskConverter.toPOJO(l, id)));
        // add a new task end job
        scheduler.scheduleSingleJob(task);
        return id;
    }

    @Override
    public void update(TaskVO taskVO) {
        int id = taskVO.getId();
        Task origin = taskMapper.get(id);
        Date now = new Date(Calendar.getInstance().getTimeInMillis());
        if(now.before(origin.getStartTime())) {
            // change everything if it's not started
            Task task = taskConverter.toPOJO(taskVO);
            taskMapper.update(task);
            labelMapper.delete(taskVO.getId());
            taskVO.getLabels().
                    forEach(l -> labelMapper.add(taskConverter.toPOJO(l, id)));
            // then update the task end job
            scheduler.scheduleSingleJob(task);
            return;
        }
        if(now.after(origin.getStartTime()) && now.before(origin.getEndTime())) {
            // change only name, description and endTime if underway
            origin.setName(taskVO.getName());
            origin.setDescription(taskVO.getDescription());
            origin.setEndTime(Date.valueOf(taskVO.getEndTime()));
            taskMapper.update(origin);
            scheduler.scheduleSingleJob(origin);
        }
        // change nothing if ended
    }

    /**
     * Just multiDelete the task. Mysql will multiDelete other related data for you.
     */
    @Override
    public void delete(int id) {
        taskMapper.delete(id);
    }

}
