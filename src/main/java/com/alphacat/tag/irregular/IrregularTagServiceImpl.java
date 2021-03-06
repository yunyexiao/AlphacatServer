package com.alphacat.tag.irregular;

import com.alphacat.mapper.IrregularTagMapper;
import com.alphacat.mapper.TaskMapper;
import com.alphacat.mapper.TaskRecordMapper;
import com.alphacat.pojo.IrregularTag;
import com.alphacat.pojo.Task;
import com.alphacat.service.IrregularTagService;
import com.alphacat.vo.IrregularTagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class IrregularTagServiceImpl implements IrregularTagService {

    @Autowired
    private IrregularTagMapper tagMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TaskRecordMapper recordMapper;
    @Autowired
    private IrregularTagConverter converter;

    @Override
    public void save(IrregularTagVO tag, int workerId, int taskId, int picIndex) {
        if(taskEnded(taskId)) {
            System.out.println("Task " + taskId + " has ended. Tags will not be saved.");
            return;
        }
        boolean exist = tagMapper.isExist(workerId, taskId, picIndex);

        if(tag != null) { // case 1. tag has data to store
            IrregularTag iTag = converter.toPOJO(tag, taskId, workerId, picIndex);

            if(!exist) { // 1A. the first time to store
                recordMapper.incPicDoneNum(workerId, taskId);
                tagMapper.add(iTag);
            } else { // 1B. not the first time
                tagMapper.update(iTag);
            }
        } else { // case 2. tag has no data, then delete
            delete(workerId, taskId, picIndex);
        }
    }

    @Override
    public void delete(int workerId, int taskId, int picIndex) {
        boolean exist = tagMapper.isExist(workerId, taskId, picIndex);
        if(!exist) {
            return;
        }
        tagMapper.delete(workerId, taskId, picIndex);
        recordMapper.decPicDoneNum(workerId, taskId);
    }

    @Override
    public IrregularTagVO get(int workerId, int taskId, int picIndex) {
        return converter.toVO(tagMapper.get(workerId, taskId, picIndex));
    }

    private boolean taskEnded(int taskId) {
        Task task = taskMapper.get(taskId);
        Calendar endTime = Calendar.getInstance();
        endTime.setTime(task.getEndTime());
        endTime.add(Calendar.DAY_OF_MONTH, 1);
        return endTime.getTime().before(Calendar.getInstance().getTime());
    }

}
