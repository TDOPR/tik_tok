package com.haoliang.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haoliang.common.config.AppParamProperties;
import com.haoliang.common.config.GlobalProperties;
import com.haoliang.common.enums.BooleanEnum;
import com.haoliang.common.enums.DataSavePathEnum;
import com.haoliang.common.enums.LanguageEnum;
import com.haoliang.common.enums.ReturnMessageEnum;
import com.haoliang.common.model.JsonResult;
import com.haoliang.common.model.PageParam;
import com.haoliang.common.model.ThreadLocalManager;
import com.haoliang.common.model.dto.PageDTO;
import com.haoliang.common.model.dto.TypeDTO;
import com.haoliang.common.model.vo.PageVO;
import com.haoliang.common.util.*;
import com.haoliang.constant.TiktokConfig;
import com.haoliang.enums.*;
import com.haoliang.mapper.TiktokTaskMapper;
import com.haoliang.mapper.TiktokTaskPriceOrdersMapper;
import com.haoliang.mapper.TiktokTaskPricesMapper;
import com.haoliang.model.*;
import com.haoliang.model.condition.TiktokTaskCondition;
import com.haoliang.model.dto.BuyPackageDTO;
import com.haoliang.model.dto.ForceDeleteDTO;
import com.haoliang.model.dto.PublishConcernTiktokTaskDTO;
import com.haoliang.model.dto.PublishTiktokTask;
import com.haoliang.model.vo.*;
import com.haoliang.service.*;
import com.haoliang.utils.EarningUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/2/24 11:35
 **/
@Slf4j
@Service
public class TiktokTaskServiceImpl extends ServiceImpl<TiktokTaskMapper, TiktokTask> implements TiktokTaskService {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private WalletsService walletsService;

    @Autowired
    private KLineDataService kLineDataService;

    @Resource
    private TiktokTaskPricesMapper tiktokTaskPricesMapper;

    @Autowired
    private AppUserTaskService appUserTaskService;

    @Resource
    private TiktokTaskPriceOrdersMapper tiktokTaskPriceOrdersMapper;

    @Autowired
    private VipOrdersService vipOrdersService;

    private HashMap<String, String> telegramMap = new HashMap<>();

    {
        telegramMap.put(LanguageEnum.ZH_CN.getName(), "https://t.me/Tiktokguild");
        telegramMap.put(LanguageEnum.EN_US.getName(), "https://t.me/Tiktokguild");
        telegramMap.put(LanguageEnum.VI_VN.getName(), "https://t.me/+B8wLTIWciow4MTNl");
        telegramMap.put(LanguageEnum.TH_TH.getName(), "https://t.me/+BpxtbYjPnVo1OTI1");
        telegramMap.put(LanguageEnum.IN_ID.getName(), "https://t.me/+55VlkhdYTKQ1NmM1");
    }

    @Override
    public JsonResult<AdvertTaskResultVO> getAdvertList() {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());

        List<AdvertTaskVO> taskVOList = this.baseMapper.randomAdvertByUserIdLimit(userId, TiktokConfig.ADVERT_LIMIT, Arrays.asList(TaskTypeEnum.IMG_ADVERT.getType(), TaskTypeEnum.VIDEO_ADVERT.getType()));
        String pre = GlobalProperties.getVirtualPathURL();
        String rootPath = GlobalProperties.getRootPath();
        AppUsers appUsers = appUserService.selectColumnsByUserId(userId, AppUsers::getVipLevel);
        VipLevelEnum vipLevelEnum = appUsers.getVipLevel().equals(VipLevelEnum.ZERO.getLevel()) ? VipLevelEnum.ONE : VipLevelEnum.getByLevel(appUsers.getVipLevel());

        BigDecimal tttToUsdRate = kLineDataService.getNowExchangeRate();

        JSONArray array;
        for (AdvertTaskVO advertTaskVO : taskVOList) {
            array = JSONObject.parseArray(advertTaskVO.getOpusId());
            advertTaskVO.setImg(pre + StringUtil.replace(array.getString(0), rootPath, ""));
            advertTaskVO.setAmount(vipLevelEnum.getAdvertEarnings().divide(tttToUsdRate, TiktokConfig.NUMBER_OF_DIGITS, RoundingMode.FLOOR).toPlainString());
        }
        //查询vip套餐大于0的数据
        List<VipOrders> ordersList = vipOrdersService.findByUserIdOrderByLevelAes(userId, appUsers.getVipLevel());

        Integer success = appUserTaskService.selectTodaySuccessAdvertNumByUserId(userId);
        BigDecimal sumAllowance = BigDecimal.ZERO;
        //获取可获手机
        for (VipOrders vipOrders : ordersList) {
            sumAllowance = sumAllowance.add(vipOrders.getAllowance());
        }
        //计算任务可获取的ttt收益
        sumAllowance = sumAllowance.divide(tttToUsdRate, TiktokConfig.NUMBER_OF_DIGITS, RoundingMode.FLOOR);
        return JsonResult.successResult(new AdvertTaskResultVO(sumAllowance.toPlainString(), success, TiktokConfig.ADVERT_LIMIT, taskVOList));
    }

    @Override
    public JsonResult<PageVO<MyTiktokTaskVO>> getMyPublishList(PageDTO pageDTO) {
        Page<MyTiktokTaskVO> tiktokTaskPage = this.baseMapper.pageByUserId(pageDTO.getPage(), JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken()), TaskTypeEnum.getOldTypeList(), TaskChannelEnum.TIKTOK.getCode());
        return JsonResult.successResult(new PageVO<>(tiktokTaskPage));
    }

    @Override
    public JsonResult<PageVO<MyTiktokTaskVO>> getMyPublishListV2(PageDTO pageDTO) {
        Page<MyTiktokTaskVO> tiktokTaskPage = this.baseMapper.pageByUserId(pageDTO.getPage(), JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken()), TaskTypeEnum.getNewTypeList(), null);
        return JsonResult.successResult(new PageVO<>(tiktokTaskPage));
    }

    @Override
    public JsonResult getPricesAndBalance() {
        if (false) {
            List<TiktokTaskPrices> tiktokTaskPricesList = tiktokTaskPricesMapper.selectList(
                    new LambdaQueryWrapper<TiktokTaskPrices>()
                            .select(TiktokTaskPrices::getId, TiktokTaskPrices::getNum, TiktokTaskPrices::getPrice)
                            .eq(TiktokTaskPrices::getVisible, BooleanEnum.TRUE.intValue())
            );
            List<TiktokTaskPricesVO> pricesVOList = new ArrayList<>(tiktokTaskPricesList.size());
            TiktokTaskPricesVO tiktokTaskPricesVO;
            for (TiktokTaskPrices tiktokTaskPrices : tiktokTaskPricesList) {
                tiktokTaskPricesVO = new TiktokTaskPricesVO();
                BeanUtils.copyProperties(tiktokTaskPrices, tiktokTaskPricesVO);
                pricesVOList.add(tiktokTaskPricesVO);
            }
            Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
            Wallets wallets = walletsService.selectColumnsByUserId(userId, Wallets::getTotalTaskNum, Wallets::getHasTaskNum);
            JSONObject object = new JSONObject();
            object.put("priceList", pricesVOList);
            object.put("balance", new TaskNumVO(wallets.getTotalTaskNum(), wallets.getHasTaskNum()));
            return JsonResult.successResult(object);
        }
        return JsonResult.failureResult(ReturnMessageEnum.PLEASE_UPDATE_VERSION);
    }

    @Override
    @Transactional
    public JsonResult buyPackage(Integer id) {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        TiktokTaskPrices tiktokTaskPrices = tiktokTaskPricesMapper.selectById(id);
        Wallets wallets = walletsService.selectColumnsByUserId(userId, Wallets::getUsdWalletAmount);
        //余额不足时候不购买
        if (wallets.getUsdWalletAmount().compareTo(new BigDecimal(tiktokTaskPrices.getPrice())) < 0) {
            return JsonResult.failureResult(ReturnMessageEnum.AMOUNT_EXCEEDS_BALANCE);
        }
        //升级成有效用户清除零撸套餐
        appUserTaskService.cleanZeroUserTask(userId);
        //购买套餐次数包
        walletsService.buyTaskNumPackage(userId, tiktokTaskPrices);
        //插入购买次数包记录
        tiktokTaskPriceOrdersMapper.insert(TiktokTaskPriceOrders.builder()
                .priceId(id)
                .userId(userId)
                .build());
        return JsonResult.successResult();
    }

    @Override
    public JsonResult newBuyPackage(BuyPackageDTO buyPackageDTO) {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        TiktokTaskPrices tiktokTaskPrices = tiktokTaskPricesMapper.selectById(buyPackageDTO.getId());
        Wallets wallets = walletsService.selectColumnsByUserId(userId, Wallets::getUsdWalletAmount, Wallets::getWalletAmount);
        BigDecimal deductionsUsd = BigDecimal.ZERO;
        if (buyPackageDTO.getTttAmount() == null) {
            buyPackageDTO.setTttAmount(0);
        }

        if (buyPackageDTO.getTttAmount() > 0) {
            if (buyPackageDTO.getTttAmount() > wallets.getWalletAmount().intValue()) {
                return JsonResult.failureResult(ReturnMessageEnum.AMOUNT_EXCEEDS_BALANCE);
            }
            //计算t币对应能抵扣的金额
            BigDecimal exchange = kLineDataService.getNowExchangeRate();
            //t币能抵扣的等值usd金额
            deductionsUsd = new BigDecimal(buyPackageDTO.getTttAmount()).multiply(exchange).setScale(TiktokConfig.NUMBER_OF_DIGITS, RoundingMode.FLOOR);
        }

        BigDecimal price = new BigDecimal(tiktokTaskPrices.getPrice());

        //减去抵扣的usd后得出实际支付的金额
        BigDecimal payUsdAmount = price.subtract(deductionsUsd);
        //有效性验证
        if (payUsdAmount.compareTo(BigDecimal.ZERO) < 0) {
            return JsonResult.failureResult();
        }

        //余额不足时候不能购买
        if (wallets.getUsdWalletAmount().compareTo(payUsdAmount) < 0 || wallets.getWalletAmount().intValue() < buyPackageDTO.getTttAmount()) {
            return JsonResult.failureResult(ReturnMessageEnum.AMOUNT_EXCEEDS_BALANCE);
        }

        //升级成有效用户清除零撸套餐
        appUserTaskService.cleanZeroUserTask(userId);

        //新版购买套餐次数包
        walletsService.newBuyTaskNumPackage(userId, tiktokTaskPrices.getNum(), payUsdAmount, buyPackageDTO.getTttAmount(),tiktokTaskPrices.getType());

        //插入购买次数包记录
        tiktokTaskPriceOrdersMapper.insert(TiktokTaskPriceOrders.builder()
                .priceId(buyPackageDTO.getId())
                .userId(userId)
                .build());

        if (payUsdAmount.compareTo(BigDecimal.ZERO) > 0) {
            AppUsers appUsers = appUserService.selectColumnsByUserId(userId, AppUsers::getInviteId);
            if (appUsers.getInviteId() != null) {
                AppUsers parentAppUsers = this.appUserService.selectColumnsByUserId(appUsers.getInviteId(), AppUsers::getId, AppUsers::getNodeLevel, AppUsers::getEnabled);
                if (parentAppUsers != null && parentAppUsers.getEnabled().equals(BooleanEnum.TRUE.intValue()) && parentAppUsers.getNodeLevel() > NodeLevelEnum.ZERO.getLevel()) {
                    NodeLevelEnum parentLevelEnum = NodeLevelEnum.getByLevel(parentAppUsers.getNodeLevel());
                    BigDecimal exchange = kLineDataService.getNowExchangeRate();
                    //发放等值的t币金额
                    BigDecimal sendAmount = payUsdAmount.multiply(parentLevelEnum.getBuyCountPackDividends()).divide(exchange, TiktokConfig.NUMBER_OF_DIGITS);
                    walletsService.updateTttWallet(sendAmount, parentAppUsers.getId(), FlowingActionEnum.INCOME, TttLogTypeEnum.POPULARIZE_ADVERT, false);
                    log.info("发放推广广告奖励: 购买套餐包用户:{} ,实际支付金额:{} USD ,上级id：{} ,上级获取的推广奖励：{} T币", userId, payUsdAmount, appUsers.getInviteId(), sendAmount);
                }
            }
        }
        return JsonResult.successResult();
    }


    @Override
    public JsonResult saveAndPublishAdvert(Integer type, Integer channel, String desc, Integer num, MultipartFile video, MultipartFile img) {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        Wallets wallets = walletsService.selectColumnsByUserId(userId, Wallets::getAdvertHasTaskNum);
        boolean checkFlag = true;

        if (img == null) {
            checkFlag = false;
        } else if (video == null && type.equals(TaskTypeEnum.VIDEO_ADVERT.getType())) {
            checkFlag = false;
        }

        if (!checkFlag) {
            return JsonResult.failureResult(ReturnMessageEnum.PARAM_CANNOT_BE_NULL);
        }

        //次数超过可用的次数则返回  次数包余额
        if (wallets.getAdvertHasTaskNum() < num) {
            return JsonResult.failureResult(ReturnMessageEnum.TASK_NUM_BALANCE);
        }

        //扣减任务包次数
        walletsService.reduceAdvertHasTaskNum(userId, num);

        //添加任务信息
        TiktokTask tiktokTask = new TiktokTask();
        tiktokTask.setChannel(channel);
        tiktokTask.setUsername(desc);
        tiktokTask.setNum(num);
        tiktokTask.setHasNum(num);
        tiktokTask.setUserId(userId);
        tiktokTask.setBuilt(BooleanEnum.FALSE.intValue());
        tiktokTask.setType(type);

        //String url = GlobalProperties.getVirtualPathURL() + StringUtil.replace(savePath, GlobalProperties.getRootPath(), "") + fileName;
        try {
            //存放广告的视频或图片
            String suffix = FileUtil.getSuffix(img.getOriginalFilename());
            String name = DateUtil.getDetailTimeIgnoreUnit();
            String fileName = name + "." + suffix;
            DataSavePathEnum dataSavePathEnum = DataSavePathEnum.ADVERT;
            String savePath = dataSavePathEnum.getPath();
            //复制图片文件流到本地文件
            FileUtils.copyInputStreamToFile(img.getInputStream(), new File(dataSavePathEnum.getFile(), fileName));
            List<String> fileList = new ArrayList<>();
            fileList.add(savePath + fileName);

            if (video != null) {
                suffix = FileUtil.getSuffix(video.getOriginalFilename());
                name = DateUtil.getDetailTimeIgnoreUnit();
                fileName = name + "." + suffix;
                //复制视频文件流到本地文件
                FileUtils.copyInputStreamToFile(video.getInputStream(), new File(dataSavePathEnum.getFile(), fileName));
                fileList.add(savePath + fileName);
            }
            tiktokTask.setOpusId(JSONObject.toJSONString(fileList));
        } catch (Exception e) {
            return JsonResult.failureResult();
        }
        this.save(tiktokTask);
        return JsonResult.successResult();
    }

    @Override
    public JsonResult<TaskNumVO> getCountPackageBalance() {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        Wallets wallets = walletsService.selectColumnsByUserId(userId, Wallets::getTotalTaskNum, Wallets::getHasTaskNum);
        return JsonResult.successResult(new TaskNumVO(wallets.getTotalTaskNum(), wallets.getHasTaskNum()));
    }

    @Override
    public JsonResult pagelist(PageParam<TiktokTask, TiktokTaskCondition> pageParam) {
        if (pageParam.getSearchParam() == null) {
            pageParam.setSearchParam(new TiktokTaskCondition());
        }
        Page<TiktokTask> page = this.page(pageParam.getPage(), pageParam.getSearchParam().buildQueryParam());
        List<AdminTiktokTaskVO> list = new ArrayList<>();
        AdminTiktokTaskVO adminTiktokTaskVO;
        JSONArray array;
        String pre = GlobalProperties.getVirtualPathURL();
        for (TiktokTask tiktokTask : page.getRecords()) {
            adminTiktokTaskVO = new AdminTiktokTaskVO();
            BeanUtils.copyProperties(tiktokTask, adminTiktokTaskVO);
            adminTiktokTaskVO.setUserUse(appUserTaskService.count(new LambdaQueryWrapper<AppUserTask>().eq(AppUserTask::getTaskId, tiktokTask.getId())) > 0);
            list.add(adminTiktokTaskVO);
            if (tiktokTask.getType() == TaskTypeEnum.IMG_ADVERT.getType() || tiktokTask.getType() == TaskTypeEnum.VIDEO_ADVERT.getType()) {
                adminTiktokTaskVO.setDesc(adminTiktokTaskVO.getUsername());
                adminTiktokTaskVO.setUsername(null);
                array = JSONArray.parseArray(tiktokTask.getOpusId());
                adminTiktokTaskVO.setImg(pre + StringUtil.replace(array.getString(0), GlobalProperties.getRootPath(), ""));
                if (tiktokTask.getType() == TaskTypeEnum.VIDEO_ADVERT.getType()) {
                    adminTiktokTaskVO.setVideo(pre + StringUtil.replace(array.getString(1), GlobalProperties.getRootPath(), ""));
                }
                adminTiktokTaskVO.setOpusId(null);
            }
        }
        return JsonResult.successResult(new PageVO<>(page.getTotal(), page.getPages(), list));
    }

    @Override
    public JsonResult addOrEdit(Long id, Integer type, Integer channel, String desc, Integer num, MultipartFile video, MultipartFile img, String username, String tiktokUserId) {
        TiktokTask tiktokTask;
        String imgPath = null, videoPath = null;
        if (id == null) {
            tiktokTask = new TiktokTask();
            tiktokTask.setBuilt(BooleanEnum.FALSE.intValue());
        } else {
            //修改
            tiktokTask = this.getById(id);
            if (type >= TaskTypeEnum.IMG_ADVERT.getType()) {
                JSONArray array = JSONObject.parseArray(tiktokTask.getOpusId());
                imgPath = array.getString(0);
                if (img != null) {
                    imgPath = null;
                    FileUtil.del(new File(imgPath));
                }
                if (array.size() > 1) {
                    videoPath = array.getString(1);
                    if (video != null) {
                        videoPath = null;
                        FileUtil.del(new File(videoPath));
                    }
                }
            }
        }

        tiktokTask.setType(type);
        tiktokTask.setChannel(channel);
        tiktokTask.setNum(num);
        tiktokTask.setHasNum(num);
        if (type >= TaskTypeEnum.IMG_ADVERT.getType()) {
            //广告任务
            tiktokTask.setUsername(desc);
            try {
                List<String> fileList = new ArrayList<>();
                DataSavePathEnum dataSavePathEnum = DataSavePathEnum.ADVERT;
                String suffix, name, fileName, savePath = dataSavePathEnum.getPath();
                //存放广告的视频或图片
                if (img != null) {
                    suffix = FileUtil.getSuffix(img.getOriginalFilename());
                    name = DateUtil.getDetailTimeIgnoreUnit();
                    fileName = name + "." + suffix;
                    //复制图片文件流到本地文件
                    FileUtils.copyInputStreamToFile(img.getInputStream(), new File(dataSavePathEnum.getFile(), fileName));
                    imgPath = savePath + fileName;
                }

                if (video != null) {
                    suffix = FileUtil.getSuffix(video.getOriginalFilename());
                    name = DateUtil.getDetailTimeIgnoreUnit();
                    fileName = name + "." + suffix;
                    //复制视频文件流到本地文件
                    FileUtils.copyInputStreamToFile(video.getInputStream(), new File(dataSavePathEnum.getFile(), fileName));
                    videoPath = savePath + fileName;
                }

                if (video != null && img == null && imgPath == null) {
                    //使用视频的首帧作为封面图
                    String saveFileName = DateUtil.getDetailTimeIgnoreUnit() + ".jpg";
                    imgPath = savePath + saveFileName;
                    ExtractVideoFirstFrameUtil.ffmpegExtractImage(videoPath, imgPath);
                }

                fileList.add(imgPath);
                if (videoPath != null) {
                    fileList.add(videoPath);
                }

                tiktokTask.setOpusId(JSONObject.toJSONString(fileList));
            } catch (Exception e) {
                return JsonResult.failureResult();
            }
        } else {
            tiktokTask.setOpusId("");
            tiktokTask.setUsername(username);
            tiktokTask.setTiktokUserId(tiktokUserId);
        }
        this.saveOrUpdate(tiktokTask);
        return JsonResult.successResult();
    }


    @Override
    public JsonResult deleteByIdList(ForceDeleteDTO forceDeleteDTO) {
        List<Long> deleteList = new ArrayList<>();
        if (forceDeleteDTO.isForce()) {
            deleteList.addAll(forceDeleteDTO.getIdList());
            appUserTaskService.remove(new LambdaQueryWrapper<AppUserTask>().eq(AppUserTask::getTaskId, deleteList));
        } else {
            for (Long id : forceDeleteDTO.getIdList()) {
                //删除之前判断任务是否被用户接取
                Long count = appUserTaskService.count(new LambdaQueryWrapper<AppUserTask>().eq(AppUserTask::getTaskId, id));
                if (count == 0) {
                    deleteList.add(id);
                }
            }
        }
        //删除tiktok任务
        this.removeByIds(deleteList);
        forceDeleteDTO.getIdList().removeAll(deleteList);
        if (forceDeleteDTO.getIdList().size() > 0) {
            return JsonResult.failureResult(ResponseStatusEnums.TASK_EXISTS_USER_USE.getCode(), String.format(ResponseStatusEnums.TASK_EXISTS_USER_USE.getName(), forceDeleteDTO.getIdList().size()), forceDeleteDTO.getIdList());
        } else {
            return JsonResult.successResult();
        }
    }


    @Override
    @Deprecated
    public JsonResult<PublishTaskVO> getPublishList(TypeDTO pageDTO, List<Integer> typeList) {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        AppUsers appUsers = appUserService.selectColumnsByUserId(userId, AppUsers::getVipLevel, AppUsers::getGreenhorn);

        VipLevelEnum vipLevelEnum;

        //可获取任务的总收益,接取每一单的任务收益
        BigDecimal sumAllowance = BigDecimal.ZERO, earnings = BigDecimal.ZERO;
        List<VipOrders> ordersList = vipOrdersService.findByUserIdOrderByLevelAes(userId, appUsers.getVipLevel());
        if (VipLevelEnum.ZERO.getLevel().equals(appUsers.getVipLevel())) {
            if (ordersList.size() > 0) {
                VipOrders vipOrders = ordersList.get(0);
                vipLevelEnum = vipOrders.getTotal().compareTo(VipLevelEnum.ZERO.getOutOfSaleAmount()) == 0 ? VipLevelEnum.ZERO : VipLevelEnum.ZERO_SECOND_MONTH;
                //当余额不足以做一单的时候,用仅有的金额去扣减
                earnings = vipOrders.getAllowance().compareTo(vipLevelEnum.getEarnings()) < 0 ? vipOrders.getAllowance() : vipLevelEnum.getEarnings();
                sumAllowance = vipOrders.getAllowance();
            } else {
                earnings = VipLevelEnum.ZERO.getEarnings();
                sumAllowance = BigDecimal.ZERO;
            }
        } else {
            //计算总共可用的usd余额
            BigDecimal oneEarning;
            Integer maxLevel = appUsers.getVipLevel();

            if (ordersList.size() > 0) {
                maxLevel = ordersList.stream().max(Comparator.comparingInt(VipOrders::getLevel)).get().getLevel();
            }

            VipLevelEnum hasLevel = VipLevelEnum.getByLevel(maxLevel);

            for (VipOrders vipOrders : ordersList) {
                sumAllowance = sumAllowance.add(vipOrders.getAllowance());
                oneEarning = EarningUtil.getEarningByAllowance(hasLevel, vipOrders);
                earnings = earnings.add(oneEarning);
            }

            if (earnings.compareTo(BigDecimal.ZERO) == 0) {
                earnings = hasLevel.getEarnings();
            }
        }
        BigDecimal tttToUsdRate = kLineDataService.getNowExchangeRate();
        //计算任务可获取的ttt收益
        sumAllowance = sumAllowance.divide(tttToUsdRate, TiktokConfig.NUMBER_OF_DIGITS, RoundingMode.FLOOR);
        //每单收益可获取的ttt额度
        BigDecimal amount = earnings.divide(tttToUsdRate, TiktokConfig.NUMBER_OF_DIGITS, RoundingMode.FLOOR);

        Page<TiktokTaskVO> tiktokTaskPage = this.baseMapper.page(pageDTO.getPage(), userId, appUsers.getGreenhorn(), pageDTO.getType(), typeList);

        for (TiktokTaskVO tiktokTask : tiktokTaskPage.getRecords()) {
            if (tiktokTask.getBamount() == null) {
                tiktokTask.setBamount(amount);
            }
            if (tiktokTask.getBuilt().equals(1)) {
                tiktokTask.setOpusId(telegramMap.get(ThreadLocalManager.getLanguage()));
            }
            //if(tiktokTask.getType().equals(TaskTypeEnum.IMG_ADVERT.getType()))
        }
        return JsonResult.successResult(new PublishTaskVO(sumAllowance, tiktokTaskPage.getTotal(), tiktokTaskPage.getPages(), tiktokTaskPage.getRecords()));
    }

    @Override
    @Deprecated
    public JsonResult publish(PublishTiktokTask publishTiktokTask) {
        if ((publishTiktokTask.getType() == TiktokTaskTypeEnum.COMMENTS.getCode() || publishTiktokTask.getType() == TiktokTaskTypeEnum.LIKE_TASK.getCode()) &&
                StringUtil.isAnyBlank(publishTiktokTask.getOpusId(), publishTiktokTask.getUsername())) {
            //点赞任务和评论任务需要输入用户名和作品Id
            return JsonResult.failureResult(ReturnMessageEnum.PARAM_CANNOT_BE_NULL);
        } else if (publishTiktokTask.getType() == TiktokTaskTypeEnum.CONCERN_TASK.getCode() && StringUtils.isAnyBlank(publishTiktokTask.getUsername(), publishTiktokTask.getTiktokUserId())) {
            //关注任务只需要输入用户名
            return JsonResult.failureResult(ReturnMessageEnum.PARAM_CANNOT_BE_NULL);
        }

        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        Wallets wallets = walletsService.selectColumnsByUserId(userId, Wallets::getHasTaskNum);

        //次数超过可用的次数则返回  次数包余额
        if (wallets.getHasTaskNum() < publishTiktokTask.getNum()) {
            return JsonResult.failureResult(ReturnMessageEnum.TASK_NUM_BALANCE);
        }

        //扣减任务包次数
        walletsService.reduceHasTaskNum(userId, publishTiktokTask.getNum());

        //添加任务信息
        TiktokTask tiktokTask = new TiktokTask();
        BeanUtils.copyProperties(publishTiktokTask, tiktokTask);
        if (StringUtil.isBlank(tiktokTask.getOpusId())) {
            tiktokTask.setOpusId("");
        }
        tiktokTask.setHasNum(tiktokTask.getNum());
        tiktokTask.setUserId(userId);
        tiktokTask.setBuilt(BooleanEnum.FALSE.intValue());
        tiktokTask.setChannel(TaskChannelEnum.TIKTOK.getCode());
        this.save(tiktokTask);
        return JsonResult.successResult();
    }

    @Override
    @Transactional
    public JsonResult publishV2(PublishConcernTiktokTaskDTO publishConcernTiktokTaskDTO) {
        if (StringUtils.isAnyBlank(publishConcernTiktokTaskDTO.getUsername(), publishConcernTiktokTaskDTO.getTiktokUserId())) {
            return JsonResult.failureResult(ReturnMessageEnum.PARAM_CANNOT_BE_NULL);
        }

        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        Wallets wallets = walletsService.selectColumnsByUserId(userId, Wallets::getHasTaskNum);

        //次数超过可用的次数则返回  次数包余额
        if (wallets.getHasTaskNum() < publishConcernTiktokTaskDTO.getNum()) {
            return JsonResult.failureResult(ReturnMessageEnum.TASK_NUM_BALANCE);
        }

        //扣减任务包次数
        walletsService.reduceHasTaskNum(userId, publishConcernTiktokTaskDTO.getNum());

        //添加任务信息
        TiktokTask tiktokTask = new TiktokTask();
        BeanUtils.copyProperties(publishConcernTiktokTaskDTO, tiktokTask);
        if (StringUtil.isBlank(tiktokTask.getOpusId())) {
            tiktokTask.setOpusId("");
        }

        tiktokTask.setHasNum(tiktokTask.getNum());
        tiktokTask.setUserId(userId);
        tiktokTask.setBuilt(BooleanEnum.FALSE.intValue());
        tiktokTask.setType(TaskTypeEnum.CONCERN.getType());
        this.save(tiktokTask);
        return JsonResult.successResult();
    }

    @Override
    public JsonResult getPricesAndBalanceV2(Integer type) {
        List<TiktokTaskPrices> tiktokTaskPricesList = tiktokTaskPricesMapper.selectList(
                new LambdaQueryWrapper<TiktokTaskPrices>()
                        .select(TiktokTaskPrices::getId, TiktokTaskPrices::getNum, TiktokTaskPrices::getPrice)
                        .eq(TiktokTaskPrices::getVisible, BooleanEnum.TRUE.intValue())
                        .eq(TiktokTaskPrices::getType,type)
        );
        List<TiktokTaskPricesVO> pricesVOList = new ArrayList<>(tiktokTaskPricesList.size());
        TiktokTaskPricesVO tiktokTaskPricesVO;
        for (TiktokTaskPrices tiktokTaskPrices : tiktokTaskPricesList) {
            tiktokTaskPricesVO = new TiktokTaskPricesVO();
            BeanUtils.copyProperties(tiktokTaskPrices, tiktokTaskPricesVO);
            pricesVOList.add(tiktokTaskPricesVO);
        }
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        Wallets wallets = walletsService.selectColumnsByUserId(userId, Wallets::getTotalTaskNum, Wallets::getHasTaskNum,Wallets::getAdvertTotalTaskNum,Wallets::getAdvertHasTaskNum);
        JSONObject object = new JSONObject();
        object.put("priceList", pricesVOList);
        if(type==TaskPricesEnum.CONCERN.getType()){
            object.put("balance", new TaskNumVO(wallets.getTotalTaskNum(), wallets.getHasTaskNum()));
        }else{
            object.put("balance", new TaskNumVO(wallets.getAdvertTotalTaskNum(), wallets.getAdvertHasTaskNum()));
        }
        return JsonResult.successResult(object);
    }

    @Override
    public JsonResult<TaskNumVO> getCountPackageBalanceV2(Integer type) {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        if(type==TaskPricesEnum.CONCERN.getType()) {
            Wallets wallets = walletsService.selectColumnsByUserId(userId, Wallets::getTotalTaskNum, Wallets::getHasTaskNum);
            return JsonResult.successResult(new TaskNumVO(wallets.getTotalTaskNum(), wallets.getHasTaskNum()));
        }else{
            Wallets wallets = walletsService.selectColumnsByUserId(userId, Wallets::getAdvertTotalTaskNum, Wallets::getAdvertHasTaskNum);
            return JsonResult.successResult(new TaskNumVO(wallets.getAdvertTotalTaskNum(), wallets.getAdvertHasTaskNum()));
        }
    }
}
