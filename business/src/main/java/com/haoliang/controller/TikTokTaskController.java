package com.haoliang.controller;

import com.haoliang.common.annotation.PrintLog;
import com.haoliang.common.annotation.RepeatSubmit;
import com.haoliang.common.model.JsonResult;
import com.haoliang.common.model.PageParam;
import com.haoliang.common.model.dto.PageDTO;
import com.haoliang.common.model.dto.TypeDTO;
import com.haoliang.common.model.vo.PageVO;
import com.haoliang.enums.TaskChannelEnum;
import com.haoliang.enums.TaskTypeEnum;
import com.haoliang.model.TiktokTask;
import com.haoliang.model.condition.AppUserTaskCondition;
import com.haoliang.model.condition.CheckTaskCondition;
import com.haoliang.model.condition.TiktokTaskCondition;
import com.haoliang.model.dto.*;
import com.haoliang.model.vo.*;
import com.haoliang.service.AppUserTaskService;
import com.haoliang.service.TiktokTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Arrays;

/**
 * @author Dominick Li
 * @Description TikTok任务发布管理
 * @CreateTime 2023/2/24 11:44
 **/
@RestController
@RequestMapping("/tiktok")
public class TikTokTaskController {

    @Autowired
    private TiktokTaskService tiktokTaskService;

    @Autowired
    private AppUserTaskService appUserTaskService;

    /**
     * 查看列表
     */
    @PostMapping("/pagelist")
    @PreAuthorize("hasAuthority('publish:tiktok:list')")
    public JsonResult pagelist(@RequestBody PageParam<TiktokTask, TiktokTaskCondition> pageParam) {
        return tiktokTaskService.pagelist(pageParam);
    }

    /**
     * 添加发布广告
     * param  id
     * @param type         任务类型 1=关注 4=图片 5=视频
     * @param channel      渠道 1=Tiktok 2=抖音
     * @param desc         描述
     * @param num          数量
     * @param video        视频
     * @param img          图片
     * @param username     用户名
     * @param userId tiktok用户ID
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('publish:tiktok:add','publish:tiktok:edit')")
    public JsonResult addOrEdit(@RequestParam(required = false) Long id, Integer type, Integer channel, @RequestParam(required = false) String desc, Integer num,
                                @RequestParam(required = false) MultipartFile video, @RequestParam(required = false) MultipartFile img,
                                @RequestParam(required = false) String username, @RequestParam(required = false) String userId) {
        return tiktokTaskService.addOrEdit(id,type, channel, desc, num, video,img, username, userId);
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('publish:tiktok:list')")
    public JsonResult delete(@RequestBody ForceDeleteDTO forceDeleteDTO) {
        return tiktokTaskService.deleteByIdList(forceDeleteDTO);
    }

    /**
     * 查看需要审核的tiktok任务
     */
    @PreAuthorize("hasAuthority('examine:tiktok:list')")
    @PostMapping("/checkTaskList")
    public JsonResult findCheckTaskList(@RequestBody PageParam<CheckTaskVO, CheckTaskCondition> pageParam) {
        return appUserTaskService.findCheckTaskList(pageParam);
    }

    /**
     * 审核
     */
    @PrintLog
    @PreAuthorize("hasAnyAuthority('examine:tiktok:pass','examine:tiktok:reject')")
    @PostMapping("/check")
    public JsonResult check(@RequestBody AuditCheckDTO auditCheckDTO) {
        return appUserTaskService.checkTask(auditCheckDTO);
    }

    /**
     * 查看广告列表
     */
    @GetMapping("/getAdvertList")
    public JsonResult<AdvertTaskResultVO> getAdvertList() {
        return tiktokTaskService.getAdvertList();
    }


    /**
     * 关注
     */
    @PostMapping("/accessTask/{id}")
    @RepeatSubmit
    public JsonResult accessTask(@PathVariable Long id) {
        return appUserTaskService.accessTask(id);
    }

    /**
     * 抢广告单
     */
    @PostMapping("/advertTask/{id}")
    @RepeatSubmit
    public JsonResult advertTask(@PathVariable Long id) {
        return appUserTaskService.advertTask(id);
    }

    /**
     * 查看广告信息
     *
     * @param type 1=广告列表跳入    2=已接单任务跳入
     * @param id
     * @return
     */
    @GetMapping("/advertDetail/{type}/{id}")
    public JsonResult advertDetail(@PathVariable Integer type, @PathVariable Long id) {
        return appUserTaskService.advertDetail(type, id);
    }

    /**
     * 获取发布任务的套餐
     */
    @GetMapping("/getPricesAndBalance")
    public JsonResult getPricesAndBalance() {
        return tiktokTaskService.getPricesAndBalance();
    }

    /**
     * 查看次数包余量
     */
    @GetMapping("/getCountPackageBalance")
    public JsonResult<TaskNumVO> getCountPackageBalance() {
        return tiktokTaskService.getCountPackageBalance();
    }

    /**
     * 购买次数包 套餐
     */
    @PostMapping("/buyPackage/{id}")
    public JsonResult buyPackage(@PathVariable Integer id) {
        return tiktokTaskService.buyPackage(id);
    }

    /**
     * 获取发布任务的套餐
     */
    @GetMapping("/getPricesAndBalance/{type}")
    public JsonResult getPricesAndBalanceV2(@PathVariable  Integer type) {
        return tiktokTaskService.getPricesAndBalanceV2(type);
    }

    /**
     * 查看次数包余量
     */
    @GetMapping("/getCountPackageBalance/{type}")
    public JsonResult<TaskNumVO> getCountPackageBalanceV2(@PathVariable  Integer type) {
        return tiktokTaskService.getCountPackageBalanceV2(type);
    }

    /**
     * 购买次数包 套餐
     */
    @PostMapping("/newBuyPackage")
    public JsonResult buyPackage(@RequestBody BuyPackageDTO buyPackageDTO) {
        return tiktokTaskService.newBuyPackage(buyPackageDTO);
    }

    /**
     * 发布广告任务
     *
     * @param type    广告类型 4=图片 5=视频
     * @param channel 渠道 1=Tiktok 2=抖音
     * @param desc    描述
     * @param num     数量
     * @param video   视频
     * @param img     视频的第一帧图片
     * @return
     */
    @PostMapping("/publishAdvert")
    public JsonResult publishAdvert(Integer type, Integer channel, String desc, Integer num,
                                    @RequestParam(required = false) MultipartFile video, MultipartFile img) {
        return tiktokTaskService.saveAndPublishAdvert(type, channel, desc, num, video, img);
    }

    /**
     * 查看自己发布的任务
     */
    @PostMapping("/getMyPublishList")
    public JsonResult<PageVO<MyTiktokTaskVO>> getMyPublishList(@RequestBody PageDTO pageDTO) {
        return tiktokTaskService.getMyPublishList(pageDTO);
    }

    /**
     * 查看自己发布的任务
     */
    @PostMapping("/getMyPublishList/v2")
    public JsonResult<PageVO<MyTiktokTaskVO>> getMyPublishListV2(@RequestBody PageDTO pageDTO) {
        return tiktokTaskService.getMyPublishListV2(pageDTO);
    }

    /**
     * 上传图片
     */
    @PostMapping("/uploadImage")
    public JsonResult uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id) {
        return appUserTaskService.uploadHeadImage(file, id);
    }

    /**
     * 已接单任务提交
     */
    @PostMapping("/submit/{id}")
    public JsonResult submit(@PathVariable Long id) {
        return appUserTaskService.submit(id);
    }

    /**
     * 查看已发布的任务  旧版本
     */
    @Deprecated
    @PostMapping("/getPublishList")
    public JsonResult<PublishTaskVO> getPublishList(@RequestBody TypeDTO typeDTO) {
        typeDTO.setType(TaskChannelEnum.TIKTOK.getCode());
        return tiktokTaskService.getPublishList(typeDTO, TaskTypeEnum.getOldTypeList());
    }

    /**
     * C端查看公会任务列表
     */
    @PostMapping("/getPublishList/v2")
    public JsonResult<PublishTaskVO> getPublishListV2(@RequestBody TypeDTO typeDTO) {
        return tiktokTaskService.getPublishList(typeDTO, Arrays.asList(TaskTypeEnum.CONCERN.getType()));
    }

    /**
     * 发布任务
     */
    @Deprecated
    @PostMapping("/publish")
    public JsonResult publish(@Valid @RequestBody PublishTiktokTask publishTiktokTask) {
        return tiktokTaskService.publish(publishTiktokTask);
    }

    /**
     * 发布关注任务
     */
    @PostMapping("/publish/v2")
    public JsonResult publishV2(@Valid @RequestBody PublishConcernTiktokTaskDTO publishConcernTiktokTaskDTO) {
        return tiktokTaskService.publishV2(publishConcernTiktokTaskDTO);
    }

    /**
     * 根据任务状态查看我的任务列表
     */
    @PostMapping("/myList")
    public JsonResult<PageVO<AppUserTaskVO>> myList(@RequestBody PageParam<AppUserTaskVO, AppUserTaskCondition> pageParam) {
        return appUserTaskService.myList(pageParam, TaskTypeEnum.getOldTypeList(), TaskChannelEnum.TIKTOK.getCode());
    }

    @PostMapping("/myList/v2")
    public JsonResult<PageVO<AppUserTaskVO>> myListV2(@RequestBody PageParam<AppUserTaskVO, AppUserTaskCondition> pageParam) {
        return appUserTaskService.myList(pageParam, TaskTypeEnum.getNewTypeList(), null);
    }

}
