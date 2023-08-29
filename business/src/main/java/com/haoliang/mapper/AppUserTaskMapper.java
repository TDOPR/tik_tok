package com.haoliang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haoliang.model.AppUserTask;
import com.haoliang.model.condition.CheckTaskCondition;
import com.haoliang.model.dto.AppUserTaskDTO;
import com.haoliang.model.dto.TiktokCountDTO;
import com.haoliang.model.dto.TiktokTaskDTO;
import com.haoliang.model.vo.AdvertTaskVO;
import com.haoliang.model.vo.AppUserTaskVO;
import com.haoliang.model.vo.CheckTaskVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppUserTaskMapper extends BaseMapper<AppUserTask> {
    Page<AppUserTaskVO> page(Page<AppUserTaskVO> page,Integer status, Integer userId,List<Integer> typeList,Integer channel);

    Page<CheckTaskVO> checkTaskPage(Page<CheckTaskVO> page, @Param("param") CheckTaskCondition searchParam);

    int selectCountByUserIdAndMaxLevel(Integer userId,Integer level);

    List<AppUserTaskDTO> getAutoCheckList(Integer status, LocalDateTime localDateTime);

    AppUserTaskDTO getById(Long id);

    TiktokTaskDTO getTiktokTaskDTO(Long id);

    List<TiktokCountDTO> selectTiktokCountNeStatus(Integer status);

    int removeTaskNeStatus(Integer status);

    Integer selectTodaySuccessAdvertNumByUserId(Integer userId);

}
