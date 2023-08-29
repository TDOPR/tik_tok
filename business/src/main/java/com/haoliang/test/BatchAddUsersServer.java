package com.haoliang.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haoliang.common.enums.BooleanEnum;
import com.haoliang.common.model.JsonResult;
import com.haoliang.common.util.IdUtil;
import com.haoliang.common.util.RandomUtil;
import com.haoliang.enums.VipLevelEnum;
import com.haoliang.mapper.TreePathMapper;
import com.haoliang.mapper.VipOrdersMapper;
import com.haoliang.model.AppUsers;
import com.haoliang.model.TreePath;
import com.haoliang.model.VipOrders;
import com.haoliang.model.Wallets;
import com.haoliang.model.dto.BatchAddSubUserDTO;
import com.haoliang.model.dto.UserTestDTO;
import com.haoliang.service.AppUserService;
import com.haoliang.service.TreePathService;
import com.haoliang.service.WalletsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/6/14 15:49
 **/
@Component
public class BatchAddUsersServer {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private TreePathService treePathService;

    @Autowired
    private WalletsService walletsService;


    @Resource
    private TreePathMapper treePathMapper;

    @Resource
    private VipOrdersMapper vipOrdersMapper;

    /**
     * 生成唯一邀请码
     */
    private String getInviteCode() {
        int start = 100000;
        int end = 999999;
        int code = RandomUtil.randomInt(start, end);
        String inviteCode = String.valueOf(code);
        AppUsers exists;
        while (true) {
            exists = appUserService.getOne(new LambdaQueryWrapper<AppUsers>().eq(AppUsers::getInviteCode, inviteCode));
            if (exists != null) {
                inviteCode = IdUtil.generateShortUUID(8);
            } else {
                break;
            }
        }
        return inviteCode;
    }

    @Transactional
    public JsonResult batchAddUser() {

        UserTestDTO u130 = new UserTestDTO(130);

        //test100的直推一代
        UserTestDTO u125 = new UserTestDTO(125);

        UserTestDTO u120 = new UserTestDTO(120);
        {
            UserTestDTO u115 = new UserTestDTO(115);
            {
                UserTestDTO u100 = new UserTestDTO(100);
                {
                    u100.setChildList(Arrays.asList(new UserTestDTO(101), new UserTestDTO(102), new UserTestDTO(103)));
                }
                UserTestDTO u111 = new UserTestDTO(111);
                {
                    u111.setChildList(Arrays.asList(new UserTestDTO(113)));
                }
                UserTestDTO u112 = new UserTestDTO(112);
                {
                    u112.setChildList(Arrays.asList(new UserTestDTO(114)));
                }
                u115.setChildList(Arrays.asList(u100, u111, u112));
            }
            UserTestDTO u116 = new UserTestDTO(116);
            {
                u116.setChildList(Arrays.asList(new UserTestDTO(118)));
            }
            UserTestDTO u117 = new UserTestDTO(117);
            {
                u117.setChildList(Arrays.asList(new UserTestDTO(119)));
            }
            u120.setChildList(Arrays.asList(u115, u116, u117, new UserTestDTO(104)));

            UserTestDTO u121 = new UserTestDTO(121);
            u121.setChildList(Arrays.asList(new UserTestDTO(123)));

            UserTestDTO u122 = new UserTestDTO(122);
            u122.setChildList(Arrays.asList(new UserTestDTO(124)));

            u125.setChildList(Arrays.asList(u120, u121, u122));
        }

        UserTestDTO u126 = new UserTestDTO(126);
        u126.setChildList(Arrays.asList(new UserTestDTO(128)));

        UserTestDTO u127 = new UserTestDTO(127);
        u127.setChildList(Arrays.asList(new UserTestDTO(129)));

        //一代
        u130.setChildList(Arrays.asList(u125, u126, u127, new UserTestDTO(131), new UserTestDTO(132), new UserTestDTO(133)));
        dg(u130, null);
        return JsonResult.successResult();
    }

    /**
     * 递归添加数据
     */
    public void dg(UserTestDTO userTestDTO, Integer inviteUserId) {
        String password = "9666635A7F04602BDF204FDC989FF63C";
        String salt = "a6de3873cc50416aae0a32722dae6aec";

        AppUsers appUsers = new AppUsers();
        appUsers.setId(userTestDTO.getUserId());
        appUsers.setEmail(appUsers.getId() + "@test.com");
        //生成邀请码
        appUsers.setInviteCode(getInviteCode());
        //设置用户的邀请人ID
        appUsers.setInviteId(inviteUserId);
        //设置密码加密用的盐
        appUsers.setSalt(salt);
        appUsers.setValid(BooleanEnum.TRUE.intValue());
        //appUsers.setVipLevel(VipLevelEnum.ONE.getLevel());
        appUsers.setPassword(password);
        appUserService.save(appUsers);

        //创建一条钱包记录
        Wallets wallets = new Wallets();
        wallets.setUsdWalletAmount(new BigDecimal("10000"));
        wallets.setUserId(appUsers.getId());
        walletsService.save(wallets);
        //添加一条默认的treepath记录
        TreePath treePath = TreePath.builder()
                .ancestor(appUsers.getId())
                .descendant(appUsers.getId())
                .level(0)
                .build();
        treePathService.save(treePath);
        vipOrdersMapper.insert(VipOrders.builder()
                .userId(appUsers.getId())
                .level(VipLevelEnum.ONE.getLevel())
                .total(VipLevelEnum.ONE.getOutOfSaleAmount())
                .allowance(VipLevelEnum.ONE.getOutOfSaleAmount())
                .build());
        if (inviteUserId != null) {
            //保存上下级关系
            treePathService.insertTreePath(appUsers.getId(), inviteUserId);
        }

        if (userTestDTO.getChildList() != null) {
            for (UserTestDTO ut : userTestDTO.getChildList()) {
                dg(ut, appUsers.getId());
            }
        }
    }

    @Transactional
    public JsonResult batchAddSubUser(BatchAddSubUserDTO batchAddSubUserDTO) {
        String password = "9666635A7F04602BDF204FDC989FF63C";
        String salt = "a6de3873cc50416aae0a32722dae6aec";
        if (batchAddSubUserDTO.getNumber() > 12000) {
            return JsonResult.failureResult("设置的用户数超出12000");
        }
        Integer userId = null;
        VipLevelEnum vipLevelEnum = VipLevelEnum.ONE;
        for (int i = 0; i < batchAddSubUserDTO.getNumber(); i++) {
            AppUsers appUsers = new AppUsers();
            //生成邀请码
            appUsers.setInviteCode(getInviteCode());
            appUsers.setEmail(appUsers.getInviteCode() + "@test.com");
            //设置用户的邀请人ID
            appUsers.setInviteId(batchAddSubUserDTO.getUserId());
            //设置密码加密用的盐
            appUsers.setSalt(salt);
            appUsers.setValid(BooleanEnum.TRUE.intValue());
            appUsers.setVipLevel(VipLevelEnum.ONE.getLevel());
            appUsers.setPassword(password);
            appUserService.save(appUsers);
            if (i == 0) {
                userId = appUsers.getId();
            }
            //创建一条钱包记录
            Wallets wallets = new Wallets();
            wallets.setUsdWalletAmount(new BigDecimal("10000"));
            wallets.setUserId(appUsers.getId());
            walletsService.save(wallets);
            //添加一条默认的treepath记录
            TreePath treePath = TreePath.builder()
                    .ancestor(appUsers.getId())
                    .descendant(appUsers.getId())
                    .level(0)
                    .build();
            treePathService.save(treePath);
            //添加套餐购买记录表中
            vipOrdersMapper.insert(VipOrders.builder()
                    .userId(appUsers.getId())
                    .total(vipLevelEnum.getOutOfSaleAmount())
                    .allowance(vipLevelEnum.getOutOfSaleAmount())
                    .level(vipLevelEnum.getLevel())
                    .build());
            //保存上下级关系
            treePathService.insertTreePath(appUsers.getId(), batchAddSubUserDTO.getUserId());
        }
        for (int i = 0; i < batchAddSubUserDTO.getSecondNumber(); i++) {
            AppUsers appUsers = new AppUsers();
            //生成邀请码
            appUsers.setInviteCode(getInviteCode());
            appUsers.setEmail(appUsers.getInviteCode() + "@test.com");
            //设置用户的邀请人ID
            appUsers.setInviteId(userId);
            //设置密码加密用的盐
            appUsers.setSalt(salt);
            appUsers.setValid(BooleanEnum.TRUE.intValue());
            appUsers.setVipLevel(VipLevelEnum.ONE.getLevel());
            appUsers.setPassword(password);
            appUserService.save(appUsers);

            //创建一条钱包记录
            Wallets wallets = new Wallets();
            wallets.setUsdWalletAmount(new BigDecimal("10000"));
            wallets.setUserId(appUsers.getId());
            walletsService.save(wallets);
            //添加一条默认的treepath记录
            TreePath treePath = TreePath.builder()
                    .ancestor(appUsers.getId())
                    .descendant(appUsers.getId())
                    .level(0)
                    .build();
            treePathService.save(treePath);
            vipOrdersMapper.insert(VipOrders.builder()
                    .userId(appUsers.getId())
                    .total(vipLevelEnum.getOutOfSaleAmount())
                    .allowance(vipLevelEnum.getOutOfSaleAmount())
                    .level(vipLevelEnum.getLevel())
                    .build());
            //保存上下级关系
            treePathService.insertTreePath(appUsers.getId(), userId);
        }
        return JsonResult.successResult();
    }

    public JsonResult addTestUsers() {
        UserTestDTO u1 = new UserTestDTO(1);
        //添加11个直推一代
        UserTestDTO u2 = new UserTestDTO(2);
        UserTestDTO u3 = new UserTestDTO(3);
        UserTestDTO u4 = new UserTestDTO(4);
        UserTestDTO u5 = new UserTestDTO(5);
        UserTestDTO u6 = new UserTestDTO(6);
        UserTestDTO u7 = new UserTestDTO(7);
        UserTestDTO u8 = new UserTestDTO(8);
        UserTestDTO u9 = new UserTestDTO(9);
        UserTestDTO u10 = new UserTestDTO(10);
        UserTestDTO u11 = new UserTestDTO(11);
        UserTestDTO u12 = new UserTestDTO(12);
        u1.setChildList(Arrays.asList(u2,u3,u4,u5,u6,u7,u8,u9,u10,u11,u12));
        //添加用户至11代
        UserTestDTO u13 = new UserTestDTO(13);
        UserTestDTO u14 = new UserTestDTO(14);
        UserTestDTO u15 = new UserTestDTO(15);
        UserTestDTO u16 = new UserTestDTO(16);
        UserTestDTO u17 = new UserTestDTO(17);
        UserTestDTO u18 = new UserTestDTO(18);
        UserTestDTO u19 = new UserTestDTO(19);
        UserTestDTO u20 = new UserTestDTO(20);
        UserTestDTO u21 = new UserTestDTO(21);
        UserTestDTO u22 = new UserTestDTO(22);
        u2.setChildList(Arrays.asList(u13));
        u13.setChildList(Arrays.asList(u14));
        u14.setChildList(Arrays.asList(u15));
        u15.setChildList(Arrays.asList(u16));
        u16.setChildList(Arrays.asList(u17));
        u17.setChildList(Arrays.asList(u18));
        u18.setChildList(Arrays.asList(u19));
        u19.setChildList(Arrays.asList(u20));
        u20.setChildList(Arrays.asList(u21));
        u21.setChildList(Arrays.asList(u22));
        dg(u1,null);
        return JsonResult.successResult();
    }
}
