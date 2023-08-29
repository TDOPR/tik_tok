package com.haoliang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haoliang.common.model.JsonResult;
import com.haoliang.common.model.PageParam;
import com.haoliang.common.model.vo.PageVO;
import com.haoliang.model.AppUserTask;
import com.haoliang.model.condition.AppUserTaskCondition;
import com.haoliang.model.condition.CheckTaskCondition;
import com.haoliang.model.dto.AuditCheckDTO;
import com.haoliang.model.vo.AppUserTaskVO;
import com.haoliang.model.vo.CheckTaskVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AppUserTaskService extends IService<AppUserTask> {

    JsonResult accessTask(Long id);

    JsonResult uploadHeadImage(MultipartFile file, Long id);

    JsonResult submit(Long id);

    JsonResult<PageVO<AppUserTaskVO>> myList(PageParam<AppUserTaskVO, AppUserTaskCondition> pageParam, List<Integer> typeList,Integer channel);

    JsonResult<PageVO<CheckTaskVO>> findCheckTaskList(PageParam<CheckTaskVO, CheckTaskCondition> pageParam);

    JsonResult checkTask(AuditCheckDTO auditCheckDTO);

    void checkTaskList();

    void cleanZeroUserTask(Integer userId);

    void removeZeroUserTask(Integer userId);

    void clearAppUserTask();

    Integer selectTodaySuccessAdvertNumByUserId(Integer userId);

    JsonResult advertTask(Long id);

    JsonResult advertDetail(Integer type,Long id);

}
