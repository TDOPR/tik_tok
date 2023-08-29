package com.haoliang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haoliang.common.model.JsonResult;
import com.haoliang.model.TreePath;
import com.haoliang.model.dto.TreePathAmountDTO;
import com.haoliang.model.dto.TreePathLevelDTO;
import com.haoliang.model.dto.TreeUserIdDTO;
import com.haoliang.model.vo.MyCommunityAdminVO;
import com.haoliang.model.vo.MyCommunityVO;

import java.util.List;

public interface TreePathService extends IService<TreePath> {

    /**
     * 获取社区最近几代的任务收益
     * @param userId 供应商Id
     * @param levelList 包含的代数
     * @return
     */
    List<TreePathAmountDTO> getTaskEarningsByUserIdAndLevel(Integer userId, Integer level, List<Integer> typeList);

    /**
     * 查询下级数量
     * @param userId 供应商Id
     */
    Integer countByAncestor(Integer userId);

    /**
     * 插入供应商 团队数据
     * @param userId 用户Id
     * @param inviteUserId 邀请人的用户Id
     */
    void insertTreePath(Integer userId, Integer inviteUserId);

    /**
     * 查询下级树结构数据
     * @param userId 用户Id
     * @return
     */
    JsonResult<List<TreeUserIdDTO>> findTreeById(Integer userId);

    /**
     * 查询树结构数据 根据级别排序
     * @param userId
     * @return
     */
    List<TreePathLevelDTO> getTreePathLevelOrderByLevel(Integer userId);

    MyCommunityVO getItemInfoByUserId(Integer userId);

    MyCommunityAdminVO getAdminItemInfoByUserId(Integer userId);

    TreePathLevelDTO getParentNodeLevel(Integer userId);

    int getGenerationUserNum(Integer userId);
}
