package com.haoliang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haoliang.common.model.JsonResult;
import com.haoliang.common.util.NumberUtil;
import com.haoliang.enums.NodeLevelEnum;
import com.haoliang.enums.TttLogTypeEnum;
import com.haoliang.enums.UsdLogTypeEnum;
import com.haoliang.mapper.TreePathMapper;
import com.haoliang.model.TreePath;
import com.haoliang.model.dto.TreePathAmountDTO;
import com.haoliang.model.dto.TreePathLevelDTO;
import com.haoliang.model.dto.TreeUserIdDTO;
import com.haoliang.model.vo.MyCommunityAdminVO;
import com.haoliang.model.vo.MyCommunityVO;
import com.haoliang.service.KLineDataService;
import com.haoliang.service.TreePathService;
import com.haoliang.service.WalletTttLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2022/11/1 12:20
 **/
@Service
public class TreePathServiceImpl extends ServiceImpl<TreePathMapper, TreePath> implements TreePathService {

    @Autowired
    private WalletTttLogsService walletTttLogsService;

    @Autowired
    private KLineDataService kLineDataService;

    @Override
    public Integer countByAncestor(Integer userId) {
        return (int) this.count(
                new LambdaQueryWrapper<TreePath>()
                        .eq(TreePath::getAncestor, userId)
                        .gt(TreePath::getLevel, 0)
        );
    }

    @Override
    public void insertTreePath(Integer id, Integer inviteUserId) {
        this.baseMapper.insertTreePath(id, inviteUserId);
    }

    @Override
    public List<TreePathAmountDTO> getTaskEarningsByUserIdAndLevel(Integer userId, Integer level, List<Integer> typeList) {
        return this.baseMapper.getTaskEarningsByUserIdAndLevel(userId, level, typeList);
    }

    @Override
    public JsonResult<List<TreeUserIdDTO>> findTreeById(Integer userId) {
        return JsonResult.successResult(this.baseMapper.findTreeById(userId));
    }

    @Override
    public List<TreePathLevelDTO> getTreePathLevelOrderByLevel(Integer userId) {
        return this.baseMapper.getTreePathLevelOrderByLevel(userId);
    }

    @Override
    public MyCommunityVO getItemInfoByUserId(Integer userId) {
        return MyCommunityVO.builder()
                .validUser(this.baseMapper.teamSumValid(userId))
                .meshUser(this.baseMapper.teamMeshUser(userId))
                .starUser(this.baseMapper.teamStarSum(userId))
                .allUser(this.baseMapper.teamSum(userId))
                .build();
    }

    @Override
    public MyCommunityAdminVO getAdminItemInfoByUserId(Integer userId) {
        Integer allUser = this.baseMapper.teamSum(userId);
        Integer vaildUser = this.baseMapper.teamMeshUser(userId);
        //团队总业绩 充值金额
        BigDecimal rechargeAmount = this.baseMapper.sumAmountByTypeAndParentId(UsdLogTypeEnum.RECHARGE.getValue(), userId);
        if (rechargeAmount == null) {
            rechargeAmount = BigDecimal.ZERO;
        }
        //团队总提现
        BigDecimal withdrawalAmount = this.baseMapper.sumAmountByTypeAndParentId(UsdLogTypeEnum.WITHDRAWAL.getValue(), userId);
        if (withdrawalAmount == null) {
            withdrawalAmount = BigDecimal.ZERO;
        }

        //汇率
        BigDecimal exchange = kLineDataService.getNowExchangeRate();

        //团队总静态收益
        BigDecimal totalStatic = walletTttLogsService.sumTotalAmountByTypeListAndParentIdHasMe(TttLogTypeEnum.getTaskTypeList(), userId);
        if (totalStatic == null) {
            totalStatic = BigDecimal.ZERO;
        }
        totalStatic = totalStatic.multiply(exchange);

        //团队总动态收益
        BigDecimal totalDynamic = walletTttLogsService.sumTotalAmountByTypeListAndParentIdHasMe(TttLogTypeEnum.getDynamicTypeList(), userId);
        if (totalDynamic == null) {
            totalDynamic = BigDecimal.ZERO;
        }
        totalDynamic = totalDynamic.multiply(exchange);

        //团队总收益
        BigDecimal total = totalStatic.add(totalDynamic);

        //获取团队的业绩
        return MyCommunityAdminVO.builder()
                .meshUser(vaildUser)
                .zeroUser(allUser - vaildUser)
                .starUser(this.baseMapper.teamStarSum(userId))
                .allUser(allUser)
                .rechargeAmount(rechargeAmount.toPlainString())
                .WithdrawalAmount(withdrawalAmount.toPlainString())
                .totalStatic(NumberUtil.downToTwoBigDecimal(totalStatic))
                .totalDynamic(NumberUtil.downToTwoBigDecimal(totalDynamic))
                .total(NumberUtil.downToTwoBigDecimal(total))
                .build();
    }

    @Override
    public TreePathLevelDTO getParentNodeLevel(Integer userId) {
        return this.baseMapper.getParentNodeLevel(userId, NodeLevelEnum.SHAREHOLDER.getLevel());
    }

    @Override
    public int getGenerationUserNum(Integer userId) {
        return this.baseMapper.getGenerationUserNum(userId);
    }
}
