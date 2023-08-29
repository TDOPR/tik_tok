package com.haoliang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haoliang.model.TiktokTask;
import com.haoliang.model.vo.AdvertTaskVO;
import com.haoliang.model.vo.MyTiktokTaskVO;
import com.haoliang.model.vo.TiktokTaskVO;

import java.util.List;

public interface TiktokTaskMapper  extends BaseMapper<TiktokTask> {

    int reduceNum(Long taskId);

    /**
     * 已兼容旧接口
     */
    Page<TiktokTaskVO> page(Page<TiktokTaskVO> page, Integer userId,Integer greenhorn,Integer channel,List<Integer> typeList);

    /**
     * 已兼容旧接口
     */
    Page<MyTiktokTaskVO> pageByUserId(Page page, Integer userId, List<Integer> typeList,Integer channel);

    void increaseNum(List<Long> idList);

    List<AdvertTaskVO> randomAdvertByUserIdLimit(Integer userId, Integer limitSize,List<Integer> typeList);


}
