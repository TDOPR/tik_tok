package com.haoliang.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haoliang.common.enums.UserTypeEnum;
import com.haoliang.common.mapper.SysLoginLogMapper;
import com.haoliang.common.model.SysLoginLog;
import com.haoliang.common.service.SysLoginLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;


@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Resource
    SysLoginLogMapper sysLoginLogMapper;

    @Override
    public Integer getTodayLoginCount() {
        return (int) sysLoginLogMapper.getTodayLoginCount(LocalDate.now().toString());
    }

    @Override
    public String getLoginIpByUserName(String email) {
        Page<SysLoginLog> page = this.page(new Page(1, 1),
                new LambdaQueryWrapper<SysLoginLog>()
                        .eq(SysLoginLog::getUsername, email)
                        .eq(SysLoginLog::getUserType, UserTypeEnum.CLIENT.getValue())
                        .orderByDesc(SysLoginLog::getCreateTime)

        );
        if (page.getTotal() > 0) {
            return page.getRecords().get(0).getIpAddr();
        }
        return "";
    }
}
