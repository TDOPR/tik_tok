package com.haoliang.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haoliang.common.enums.BooleanEnum;
import com.haoliang.common.enums.LanguageEnum;
import com.haoliang.common.enums.ReturnMessageEnum;
import com.haoliang.common.model.JsonResult;
import com.haoliang.common.model.ThreadLocalManager;
import com.haoliang.common.util.JwtTokenUtil;
import com.haoliang.common.util.MessageUtil;
import com.haoliang.common.util.NumberUtil;
import com.haoliang.constant.TiktokConfig;
import com.haoliang.enums.*;
import com.haoliang.mapper.*;
import com.haoliang.model.*;
import com.haoliang.model.dto.*;
import com.haoliang.model.usd.*;
import com.haoliang.model.vo.*;
import com.haoliang.pay.enums.CoinUnitEnum;
import com.haoliang.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2022/11/1 12:20
 **/
@Slf4j
@Service
public class WalletsServiceImpl extends ServiceImpl<WalletsMapper, Wallets> implements WalletsService {

    @Autowired
    private WalletTttLogsService walletTttLogsService;

    @Autowired
    private WalletUsdLogsService walletUsdLogsService;

    @Autowired
    private KLineDataService kLineDataService;

    @Autowired
    private TreePathService treePathService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private FreezeProxyLogsService freezeProxyLogsService;

    @Autowired
    private UpdateUserLevelTaskService updateUserLevelTaskService;

    @Autowired
    private AppUserTaskService appUserTaskService;

    @Autowired
    private BuyNodeDelayAmountService buyNodeDelayAmountService;

    @Resource
    private VipOrdersMapper vipOrdersMapper;

    @Resource
    private TrxAddressPoolMapper trxAddressPoolMapper;

    @Resource
    private TrxUserWalletMapper trxUserWalletMapper;

    private HashMap<String, String> customerMap = new HashMap<>();

    {
        customerMap.put(LanguageEnum.ZH_CN.getName(), "https://t.me/TTG002");
        customerMap.put(LanguageEnum.VI_VN.getName(), "https://t.me/TTG003");
        customerMap.put(LanguageEnum.EN_US.getName(), "https://t.me/TTG003");
        customerMap.put(LanguageEnum.TH_TH.getName(), "https://t.me/TTG004");
        customerMap.put(LanguageEnum.IN_ID.getName(), "https://t.me/TTG005");
    }

    public HashMap<String, String> getCustomerMap() {
        return customerMap;
    }

    @Override
    @Transactional
    public JsonResult<MyWalletsVO> getMyWallet() {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        Wallets wallets = this.selectColumnsByUserId(userId, Wallets::getWalletAmount, Wallets::getUsdWalletAmount);

        //获取我的代理收益
        EarningsDTO earningsDTO = walletTttLogsService.getMyEarningsWalletLogs(userId);

        //加载货币信息
        CoinInfoVo coinInfoVo = new CoinInfoVo();
        TrxUserWallet trxUserWallet = getAndSetBlockAddress(userId, CoinNetworkSourceEnum.TRX.getName());
        String trxAddress = trxUserWallet != null ? trxUserWallet.getAddress() : null;

        List<BlockAddressVo> blockAddressList = new ArrayList<>();
        blockAddressList.add(new BlockAddressVo(trxAddress, CoinNetworkSourceEnum.TRX));
        coinInfoVo.setBlockAddressList(blockAddressList);

        //查询节点已释放和待释放的t币
        List<BuyNodeDelayAmount> buyNodeDelayAmountList = buyNodeDelayAmountService.list(new LambdaQueryWrapper<BuyNodeDelayAmount>()
                .eq(BuyNodeDelayAmount::getUserId, userId));
        int total = 0, nodeReleased = 0, nodeToBeReleased;
        for (BuyNodeDelayAmount buyNodeDelayAmount : buyNodeDelayAmountList) {
            total += buyNodeDelayAmount.getAmount();
            nodeReleased += buyNodeDelayAmount.getAmount() / TiktokConfig.DELAY_SEND_DAY * buyNodeDelayAmount.getDay();
        }
        //待释放的节点奖励
        nodeToBeReleased = total - nodeReleased;

        return JsonResult.successResult(MyWalletsVO.builder()
                .communityBenefits(NumberUtil.toPlainString(earningsDTO.getCommunityBenefits()))
                .taskBenefits(NumberUtil.toPlainString(earningsDTO.getTaskBenefits()))
                .coinInfo(coinInfoVo)
                .customer(customerMap.get(ThreadLocalManager.getLanguage()))
                .community(treePathService.getItemInfoByUserId(userId))
                .supportFiat(TiktokSettingEnum.SUPPORT_FIAT.boolValue())
                .toBeClaimed(NumberUtil.toPlainString(freezeProxyLogsService.getRecentOneWeek(userId)))
                .usdBalance(NumberUtil.toPlainString(wallets.getUsdWalletAmount()))
                .tttBalance(NumberUtil.toPlainString(wallets.getWalletAmount()))
                .nodeReleased(NumberUtil.toPlainString(new BigDecimal(nodeReleased)))
                .nodeToBeReleased(NumberUtil.toPlainString(new BigDecimal(nodeToBeReleased)))
                .build());
    }

    /**
     * 为用户分配一条区块链钱包
     */
    @Transactional
    public TrxUserWallet getAndSetBlockAddress(Integer userId, String networkName) {
        TrxUserWallet trxUserWallet = trxUserWalletMapper.selectOne(new LambdaQueryWrapper<TrxUserWallet>().eq(TrxUserWallet::getUserId, userId));
        if (trxUserWallet == null) {
            TrxAddressPool trxAddressPool = trxAddressPoolMapper.randomGetAddress(networkName);
            if (trxAddressPool != null) {
                //删除地址
                trxAddressPoolMapper.deleteByAddress(trxAddressPool.getAddress());
                //添加到区块链用户钱包表
                trxUserWallet = TrxUserWallet.builder()
                        .address(trxAddressPool.getAddress())
                        .coinId(trxAddressPool.getCoinId())
                        .keystore(trxAddressPool.getKeystore())
                        .valid("E")
                        .lowerAddress(trxAddressPool.getAddress())
                        .password(trxAddressPool.getPwd())
                        .userId(userId)
                        .build();
                trxUserWalletMapper.insert(trxUserWallet);
            }
        }
        return trxUserWallet;
    }


    /**
     * 根据用户ID查询指定列数据
     *
     * @param userId  用户Id
     * @param columns 需要查询的指定列
     * @return
     */
    @Override
    public Wallets selectColumnsByUserId(Integer userId, SFunction<Wallets, ?>... columns) {
        Wallets wallets = this.getOne(new LambdaQueryWrapper<Wallets>().select(columns).eq(Wallets::getUserId, userId));
        return wallets;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTttWallet(BigDecimal amount, Integer userId, FlowingActionEnum flowingActionEnum, TttLogTypeEnum tttLogTypeEnum, boolean zero) {
        boolean flag = this.lookUpdateWallets(userId, amount, flowingActionEnum);
        if (flag) {
            //插入流水记录
            if (zero) {
                walletTttLogsService.insertZeroWalletLogs(userId, amount, flowingActionEnum, tttLogTypeEnum);
            } else {
                walletTttLogsService.insertWalletLogs(userId, amount, flowingActionEnum, tttLogTypeEnum);
            }
        }
        return flag;
    }

    @Override
    public boolean updateUsdWallet(BigDecimal amount, Integer userId, FlowingActionEnum flowingActionEnum, UsdLogTypeEnum usdLogTypeEnum) {
        boolean flag = this.lookUsdUpdateWallets(userId, amount, flowingActionEnum);
        if (flag) {
            //插入流水记录
            walletUsdLogsService.insertWalletLogs(userId, amount, flowingActionEnum, usdLogTypeEnum);
        }
        return flag;
    }

    @Override
    public boolean updateUsdWallet(BigDecimal amount, Integer userId, FlowingActionEnum flowingActionEnum, UsdLogTypeEnum usdLogTypeEnum, CoinUnitEnum coinUnitEnum) {
        boolean flag = this.lookUsdUpdateWallets(userId, amount, flowingActionEnum);
        if (flag) {
            //插入流水记录
            walletUsdLogsService.insertWalletLogs(userId, amount, flowingActionEnum, usdLogTypeEnum, coinUnitEnum);
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lookUsdUpdateWallets(Integer userId, BigDecimal amount, FlowingActionEnum flowingActionEnum) {
        int ret;
        if (flowingActionEnum.equals(FlowingActionEnum.INCOME)) {
            ret = this.baseMapper.lockUpdateAddUsdWallet(userId, amount);
        } else {
            //减
            ret = this.baseMapper.lockUpdateReduceUsdWallet(userId, amount);
        }
        return ret == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lookUpdateWallets(Integer userId, BigDecimal amount, FlowingActionEnum flowingActionEnum) {
        int ret;
        if (flowingActionEnum.equals(FlowingActionEnum.INCOME)) {
            ret = this.baseMapper.lockUpdateAddWallet(userId, amount);
        } else {
            //减
            ret = this.baseMapper.lockUpdateReduceWallet(userId, amount);
        }
        return ret == 1;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unFrozenAmount(Integer userId, BigDecimal amount, Long usdLogId, CoinUnitEnum coinUnitEnum) {
        boolean flag = this.baseMapper.unFrozenAmount(userId, amount) == 1;
        if (flag) {
            //添加提现驳回记录
            WalletUsdLogs walletLogs = WalletUsdLogs.builder()
                    .userId(userId)
                    .amount(amount)
                    .status(WithdrawCheckStatusEnum.REJECT.getStatus())
                    .action(FlowingActionEnum.INCOME.getValue())
                    .type(UsdLogTypeEnum.WITHDRAWAL.getValue())
                    .coinId(coinUnitEnum.getId())
                    .build();
            this.walletUsdLogsService.save(walletLogs);
            //修改之前的流水状态从审核中为成功0
            if (usdLogId != null) {
                UpdateWrapper<WalletUsdLogs> updateWrapper = Wrappers.update();
                updateWrapper.lambda()
                        .set(WalletUsdLogs::getStatus, WithdrawCheckStatusEnum.QUASH.getStatus())
                        .eq(WalletUsdLogs::getId, usdLogId);
                walletUsdLogsService.update(updateWrapper);
            }
            log.info("取消冻结的提现金额给用户: usdLogId={}", usdLogId);
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long frozenAmount(Integer userId, BigDecimal amount, CoinUnitEnum coinUnitEnum) {
        boolean flag = this.baseMapper.frozenAmount(userId, amount) == 1;
        if (flag) {
            //添加提现审核中数据
            WalletUsdLogs walletLogs = WalletUsdLogs.builder()
                    .userId(userId)
                    .amount(amount)
                    .status(WithdrawCheckStatusEnum.UNDER_REVIEW.getStatus())
                    .action(FlowingActionEnum.EXPENDITURE.getValue())
                    .type(UsdLogTypeEnum.WITHDRAWAL.getValue())
                    .coinId(coinUnitEnum.getId())
                    .build();
            this.walletUsdLogsService.save(walletLogs);
            return walletLogs.getId();
        }
        return null;
    }

    @Override
    public boolean reduceFrozenAmount(Integer userId, BigDecimal amount, Long usdLogId) {
        boolean flag = this.baseMapper.reduceFrozenAmount(userId, amount) == 1;
        if (flag) {
            //修改之前的流水状态从审核中为成功0
            if (usdLogId != null) {
                UpdateWrapper<WalletUsdLogs> updateWrapper = Wrappers.update();
                updateWrapper.lambda()
                        .set(WalletUsdLogs::getStatus, WithdrawCheckStatusEnum.SUCCESS.getStatus())
                        .eq(WalletUsdLogs::getId, usdLogId);
                walletUsdLogsService.update(updateWrapper);
            }
            log.info("打币成功 修改流水状态为成功: usdLogId={}", usdLogId);
        }
        return flag;
    }

    @Override
    public JsonResult getBlockAddress(String networdName) {
        CoinNetworkSourceEnum coinNetworkSourceEnum = CoinNetworkSourceEnum.networdNameOf(networdName);
        if (coinNetworkSourceEnum == null) {
            return JsonResult.failureResult(ReturnMessageEnum.UB_SUPPORT_NETWORD);
        }
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        String address = null;
        if (coinNetworkSourceEnum == CoinNetworkSourceEnum.TRX) {
            TrxUserWallet trxUserWallet = trxUserWalletMapper.selectOne(new LambdaQueryWrapper<TrxUserWallet>().eq(TrxUserWallet::getUserId, userId));
            if (trxUserWallet == null) {
                TrxAddressPool trxAddressPool = trxAddressPoolMapper.randomGetAddress(coinNetworkSourceEnum.getName());
                if (trxAddressPool != null) {
                    //删除地址
                    trxAddressPoolMapper.deleteByAddress(trxAddressPool.getAddress());
                    //添加到区块链用户钱包表
                    trxUserWallet = TrxUserWallet.builder()
                            .address(trxAddressPool.getAddress())
                            .coinId(trxAddressPool.getCoinId())
                            .keystore(trxAddressPool.getKeystore())
                            .valid("E")
                            .lowerAddress(trxAddressPool.getAddress())
                            .password(trxAddressPool.getPwd())
                            .userId(userId)
                            .build();
                    trxUserWalletMapper.insert(trxUserWallet);
                }
            }
            if (trxUserWallet != null) {
                address = trxUserWallet.getAddress();
            }
        }
        if (address != null) {
            JSONObject data = new JSONObject();
            //如果已存在该网络地址,则使用已存在的
            data.put("address", address);
            return JsonResult.successResult(data);
        }
        log.error("地址池没有 {}充值网络的地址", networdName);
        return JsonResult.failureResult();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyTaskNumPackage(Integer userId, TiktokTaskPrices tiktokTaskPrices) {
        this.baseMapper.buyTaskNumPackage(userId, tiktokTaskPrices.getPrice(), tiktokTaskPrices.getNum());
        //插入支付流水记录
        walletUsdLogsService.insertWalletLogs(userId, new BigDecimal(tiktokTaskPrices.getPrice()), FlowingActionEnum.EXPENDITURE, UsdLogTypeEnum.BUY_TASK_NUM_PACKAGE);
        UpdateWrapper<AppUsers> updateWrapper = Wrappers.update();
        updateWrapper.lambda()
                .set(AppUsers::getValid, BooleanEnum.TRUE.intValue())
                .eq(AppUsers::getId, userId);
        appUserService.update(updateWrapper);
        updateUserLevelTaskService.insertListByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void newBuyTaskNumPackage(Integer userId, Integer num, BigDecimal payUsdAmount, Integer tttAmount, Integer type) {
        BigDecimal tAmount = new BigDecimal(tttAmount);
        //根据套餐包类型去修改对应的钱包次数
        if (type == TaskPricesEnum.CONCERN.getType()) {
            this.baseMapper.newBuyTaskNumPackage(userId, payUsdAmount, num, tAmount);
        } else {
            this.baseMapper.buyAdvertTaskNumPackage(userId, payUsdAmount, num, tAmount);
        }
        //插入支付流水记录
        walletUsdLogsService.insertWalletLogs(userId, payUsdAmount, FlowingActionEnum.EXPENDITURE, UsdLogTypeEnum.BUY_TASK_NUM_PACKAGE);
        if (tttAmount > 0) {
            //插入抵扣金额流水
            walletTttLogsService.insertWalletLogs(userId, tAmount, FlowingActionEnum.EXPENDITURE, TttLogTypeEnum.BUY_TASK_NUM_PACKAGE);
        }
        UpdateWrapper<AppUsers> updateWrapper = Wrappers.update();
        updateWrapper.lambda()
                .set(AppUsers::getValid, BooleanEnum.TRUE.intValue())
                .eq(AppUsers::getId, userId);
        appUserService.update(updateWrapper);
        updateUserLevelTaskService.insertListByUserId(userId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reduceHasTaskNum(Integer userId, Integer num) {
        this.baseMapper.reduceHasTaskNum(userId, num);
    }


    @Override
    public JsonResult<List<VipLevelVO>> getVipList() {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        AppUsers appUsers = appUserService.getOne(new LambdaQueryWrapper<AppUsers>()
                .select(AppUsers::getVipLevel, AppUsers::getValid)
                .eq(AppUsers::getId, userId)
        );
        Wallets wallets = this.selectColumnsByUserId(userId, Wallets::getWalletAmount);
        //根据TTT余额金额转换成USD
        BigDecimal usd = wallets.getWalletAmount().multiply(kLineDataService.getNowExchangeRate()).setScale(TiktokConfig.NUMBER_OF_DIGITS, RoundingMode.FLOOR);
        return JsonResult.successResult(appUsers.getValid().equals(BooleanEnum.FALSE.intValue()) ? VipLevelEnum.getVipListByZero(usd) : VipLevelEnum.getVipList(appUsers.getVipLevel()));
    }

    @Override
    @Transactional
    public JsonResult buyVip(BuyLevelDTO buyLevelDTO) {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        VipLevelEnum vipLevelEnum = VipLevelEnum.getByLevel(buyLevelDTO.getLevel());
        if (vipLevelEnum == null) {
            return JsonResult.failureResult();
        }

        //判断钱包里的Usd余额是否可以购买vip
        Wallets wallets = this.selectColumnsByUserId(userId, Wallets::getWalletAmount, Wallets::getUsdWalletAmount);

        //vip购买金额
        BigDecimal buyVipAmount = vipLevelEnum.getAmount();

        //判断是否新手抵扣金额
        AppUsers appUsers = appUserService.getById(userId);

        //零撸用户购买vip使用ttt的抵扣金额
        BigDecimal deductionsAmount = BigDecimal.ZERO;
        //零撸用户第一次购买可以用t币抵扣金额
        if (appUsers.getValid().equals(BooleanEnum.FALSE.intValue())) {
            //根据TTT余额金额转换成USD
            BigDecimal usd = wallets.getWalletAmount().multiply(kLineDataService.getNowExchangeRate()).setScale(TiktokConfig.NUMBER_OF_DIGITS, RoundingMode.FLOOR);
            //根据购买的vip等级计算能抵扣的金额
            if (vipLevelEnum.getLevel().equals(VipLevelEnum.ONE.getLevel())) {
                deductionsAmount = TiktokConfig.V1_USD.compareTo(usd) > 0 ? usd : TiktokConfig.V1_USD;
            } else if (vipLevelEnum.getLevel().equals(VipLevelEnum.TWO.getLevel())) {
                deductionsAmount = TiktokConfig.V2_USD.compareTo(usd) > 0 ? usd : TiktokConfig.V2_USD;
            } else {
                deductionsAmount = usd;
            }
        }

        //实际支付=vip购买金额-抵扣金额
        BigDecimal payAmount = buyVipAmount.subtract(deductionsAmount);

        //usd钱包小于支付金额 则返回提示信息
        if (wallets.getUsdWalletAmount().compareTo(payAmount) < 0) {
            return JsonResult.failureResult(ReturnMessageEnum.AMOUNT_EXCEEDS_BALANCE);
        }

        //扣usd费用
        if (payAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.updateUsdWallet(payAmount, userId, FlowingActionEnum.EXPENDITURE, UsdLogTypeEnum.BUY_VIP);
        }

        if (deductionsAmount.compareTo(BigDecimal.ZERO) > 0) {
            //扣减TTT抵扣的金额
            BigDecimal tttAmount = deductionsAmount.divide(kLineDataService.getNowExchangeRate(), TiktokConfig.NUMBER_OF_DIGITS, RoundingMode.FLOOR);
            this.updateTttWallet(tttAmount, userId, FlowingActionEnum.EXPENDITURE, TttLogTypeEnum.BUY_VIP, false);
        }

        //升级成有效用户清除零撸套餐
        appUserTaskService.cleanZeroUserTask(userId);

        //更新用户的vip等级
        UpdateWrapper<AppUsers> updateWrapper = Wrappers.update();
        updateWrapper.lambda().
                set(AppUsers::getVipLevel, buyLevelDTO.getLevel())
                .set(AppUsers::getValid, BooleanEnum.TRUE.intValue())
                .eq(AppUsers::getId, userId);
        appUserService.update(updateWrapper);
        updateUserLevelTaskService.insertListByUserId(userId);
        //添加套餐购买记录表中
        vipOrdersMapper.insert(VipOrders.builder()
                .userId(userId)
                .total(vipLevelEnum.getOutOfSaleAmount())
                .allowance(vipLevelEnum.getOutOfSaleAmount())
                .level(vipLevelEnum.getLevel())
                .build());
        return JsonResult.successResult();
    }

    @Override
    @Transactional
    public JsonResult buyNodeLevel(BuyLevelDTO buyLevelDTO) {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        NodeLevelEnum buyNodeLevelEnum = NodeLevelEnum.getByLevel(buyLevelDTO.getLevel());
        if (buyNodeLevelEnum == null || buyNodeLevelEnum == NodeLevelEnum.ZERO) {
            return JsonResult.failureResult();
        }
        //获取用户的社区节点等级
        AppUsers appUsers = appUserService.selectColumnsByUserId(userId, AppUsers::getNodeLevel, AppUsers::getLevel, AppUsers::getInviteId);
        //完整性校验
        if (appUsers.getNodeLevel().equals(buyLevelDTO.getLevel()) || appUsers.getNodeLevel() > buyLevelDTO.getLevel()) {
            return JsonResult.failureResult(ReturnMessageEnum.NO_REPEAT_BUY);
        }

        Long count = appUserService.count(
                new LambdaQueryWrapper<AppUsers>()
                        .eq(AppUsers::getEnabled, BooleanEnum.TRUE.intValue())
                        .eq(AppUsers::getNodeLevel, buyLevelDTO.getLevel())
        );

        //如果节点名称已经被购买完则不能再被购买
        if (count >= buyNodeLevelEnum.getLimitSize()) {
            return JsonResult.failureResult(ReturnMessageEnum.BUY_NODE_LIMIT);
        }

        BigDecimal buyAmount = new BigDecimal(buyNodeLevelEnum.getAmount());
        //赠送的t币
        int giveTttAmount = buyNodeLevelEnum.getGiveTttAmount();

        //判断是否补差价升级成股东节点
        if (appUsers.getNodeLevel().equals(NodeLevelEnum.COMMUNITY.getLevel()) && buyLevelDTO.getLevel().equals(NodeLevelEnum.SHAREHOLDER.getLevel())) {
            //扣掉抵扣价
            buyAmount = new BigDecimal(buyNodeLevelEnum.getAmount() - NodeLevelEnum.COMMUNITY.getAmount());
            giveTttAmount -= NodeLevelEnum.COMMUNITY.getGiveTttAmount();
        }

        //判断钱包里的Usd余额是否可以购买社区节点
        Wallets wallets = this.selectColumnsByUserId(userId, Wallets::getUsdWalletAmount);
        if (wallets.getUsdWalletAmount().compareTo(buyAmount) < 0) {
            return JsonResult.failureResult(ReturnMessageEnum.AMOUNT_EXCEEDS_BALANCE);
        }

        if (appUsers.getInviteId() != null) {
            AppUsers parentAppUsers = this.appUserService.selectColumnsByUserId(appUsers.getInviteId(), AppUsers::getId, AppUsers::getNodeLevel, AppUsers::getEnabled);
            if (parentAppUsers != null && parentAppUsers.getEnabled().equals(BooleanEnum.TRUE.intValue())) {
                //如果直属邀请人是个节点,则发放节点推广奖
                if (parentAppUsers.getNodeLevel() > NodeLevelEnum.ZERO.getLevel()) {
                    //发放购买金额等值的t币给上级节点
                    NodeLevelEnum parentLevelEnum = NodeLevelEnum.getByLevel(parentAppUsers.getNodeLevel());
                    if (parentLevelEnum != null) {
                        //发放等值的t币金额
                        BigDecimal sendAmount = new BigDecimal(giveTttAmount).multiply(parentLevelEnum.getBuyNodeDividends());
                        //this.updateTttWallet(sendAmount, parentAppUsers.getId(), FlowingActionEnum.INCOME, TttLogTypeEnum.RECOMMEND_BUY_NODE_LEVEL, false);
                        log.info("添加推广购买节点延迟奖励: 购买节点用户:{} ,支付金额:{} USD ,上级id：{} ,上级获取的推广奖励：{} T币", userId, buyAmount, appUsers.getInviteId(), sendAmount);
                        buyNodeDelayAmountService.save(BuyNodeDelayAmount.builder()
                                .userId(parentAppUsers.getId())
                                .type(BuyNodeDelayAmountTypeEnum.PROMOTION.getType())
                                .amount(sendAmount.intValue())
                                .subUserId(userId)
                                .build());
                    }
                }
            }
        }

        log.info("用户:{},购买:{},消费金额:{},赠送T币:{}", userId, buyNodeLevelEnum.getName(), buyAmount, giveTttAmount);
        this.updateUsdWallet(buyAmount, userId, FlowingActionEnum.EXPENDITURE, UsdLogTypeEnum.BUY_NODE);

        //this.updateTttWallet(new BigDecimal(giveTttAmount), userId, FlowingActionEnum.INCOME, TttLogTypeEnum.BUY_NODE_LEVEL_INPUT, false);

        //添加用户购买节点延迟发放奖励
        log.info("添加购买节点延迟奖励: 购买节点用户:{} ,支付金额:{} USD ,获取的购买节点延迟奖励：{} T币", userId, buyAmount, giveTttAmount);
        buyNodeDelayAmountService.save(BuyNodeDelayAmount.builder()
                .userId(userId)
                .type(BuyNodeDelayAmountTypeEnum.BUY.getType())
                .amount(giveTttAmount)
                .build());

        //升级成有效用户清除零撸套餐
        appUserTaskService.cleanZeroUserTask(userId);

        //更新用户的社区等级
        UpdateWrapper<AppUsers> updateWrapper = Wrappers.update();
        updateWrapper.lambda()
                .set(AppUsers::getNodeLevel, buyLevelDTO.getLevel())
                .set(AppUsers::getValid, BooleanEnum.TRUE.intValue())
                .eq(AppUsers::getId, userId);
        if (appUsers.getLevel() < buyNodeLevelEnum.getCommunityLevel()) {
            //如果用户的社区等级小于买节点送的社区等级则升级
            updateWrapper.lambda()
                    .set(AppUsers::getLevel, buyNodeLevelEnum.getCommunityLevel());
        }
        appUserService.update(updateWrapper);

        return JsonResult.successResult();
    }

    @Override
    public JsonResult<TttWalletInfoVO> getTttWalletInfo() {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        //获取最新的TTT换USD汇率
        BigDecimal exchangeRate = kLineDataService.getNowExchangeRate();
        //获取历史总收益
        BigDecimal total = walletTttLogsService.sumTotalEarnings(userId);
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        Wallets wallets = this.selectColumnsByUserId(userId, Wallets::getUsdWalletAmount, Wallets::getWalletAmount);
        return JsonResult.successResult(TttWalletInfoVO.builder()
                .tttBalance(NumberUtil.toPlainString(wallets.getWalletAmount()))
                .tttBalanceUsd(NumberUtil.toPlainString(wallets.getWalletAmount().multiply(exchangeRate).setScale(TiktokConfig.NUMBER_OF_DIGITS, RoundingMode.FLOOR)))
                .usdBalance(NumberUtil.toPlainString(wallets.getUsdWalletAmount()))
                .exchangeRate(NumberUtil.toPlainString(exchangeRate))
                .historyEarningsTtt(NumberUtil.toPlainString(total))
                .historyEarningsUsd(NumberUtil.toPlainString(total.multiply(exchangeRate).setScale(TiktokConfig.NUMBER_OF_DIGITS, RoundingMode.FLOOR)))
                .build());
    }

    @Override
    @Transactional
    public JsonResult tttConversionUsd(AmountDTO amountDTO) {
        Integer userId = JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken());
        BigDecimal exchangeRate = kLineDataService.getNowExchangeRate();
        if (exchangeRate.compareTo(BigDecimal.ZERO) == 0) {
            return JsonResult.failureResult();
        }

        AppUsers appUsers = appUserService.selectColumnsByUserId(userId, AppUsers::getVipLevel);
        if (VipLevelEnum.ZERO.getLevel().equals(appUsers.getVipLevel())) {
            //如果用户没有购买vip等级,需要判断直推用户数是有3位
            Long count = appUserService.count(new LambdaQueryWrapper<AppUsers>().eq(AppUsers::getInviteId, userId).eq(AppUsers::getValid, BooleanEnum.TRUE.intValue()));
            if (count < TiktokConfig.MIN_USER_COUNT) {
                //小于3位则提示错误信息
                return JsonResult.failureResult(ResponseStatusEnums.ZERO_LEVEL_USER_LIMIT_ERROR.getCode(), ResponseStatusEnums.ZERO_LEVEL_USER_LIMIT_ERROR.getMsg());
            }
        }

        //查询账号余额
        Wallets wallets = this.selectColumnsByUserId(userId, Wallets::getWalletAmount);
        if (amountDTO.getAmount().compareTo(wallets.getWalletAmount()) > 0) {
            return JsonResult.failureResult(ReturnMessageEnum.AMOUNT_EXCEEDS_BALANCE);
        }
        //计算出转换后的Usd金额
        BigDecimal exchangeUsd = amountDTO.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.FLOOR);
        if (exchangeUsd.compareTo(new BigDecimal("0.01")) < 0) {
            return JsonResult.failureResult(ReturnMessageEnum.MIN_USD_ERROR);
        }
        //修改数据库金额
        this.baseMapper.tttConversionUsd(userId, amountDTO.getAmount(), exchangeUsd);
        //插入TTT账单明细流水
        walletTttLogsService.insertWalletLogs(userId, amountDTO.getAmount(), FlowingActionEnum.EXPENDITURE, TttLogTypeEnum.TO_USD);
        //插入USD账单明细流水
        walletUsdLogsService.insertWalletLogs(userId, exchangeUsd, FlowingActionEnum.INCOME, UsdLogTypeEnum.TTT_TRANSFER_IN);
        return JsonResult.successResult();
    }

    @Override
    public List<AppUsersAmountDTO> findHoldingCoinUserInfo() {
        return this.baseMapper.findHoldingCoinUserInfo();
    }

    @Override
    public JsonResult<List<NodeLevelVO>> getNodeLevelList() {
        String language = ThreadLocalManager.getLanguage();
        List<NodeLevelVO> list = new ArrayList<>();
        Long count;
        int payAmount;
        List<String> textList;
        boolean has;
        //查询当前购买节点用户是否买过社区节点
        AppUsers appUsers = appUserService.selectColumnsByUserId(JwtTokenUtil.getUserIdFromToken(ThreadLocalManager.getToken()), AppUsers::getNodeLevel);
        NodeLevelEnum hasLevelEnum = NodeLevelEnum.getByLevel(appUsers.getNodeLevel());
        for (NodeLevelEnum nodeLevelEnum : NodeLevelEnum.values()) {
            if (nodeLevelEnum == NodeLevelEnum.ZERO) {
                continue;
            }
            count = appUserService.count(
                    new LambdaQueryWrapper<AppUsers>()
                            .eq(AppUsers::getEnabled, BooleanEnum.TRUE.intValue())
                            .eq(AppUsers::getNodeLevel, nodeLevelEnum.getLevel())
            );
            payAmount = nodeLevelEnum.getAmount();
            if (nodeLevelEnum.getLevel() > hasLevelEnum.getLevel() && hasLevelEnum.getLevel() > NodeLevelEnum.ZERO.getLevel()) {
                payAmount = payAmount - hasLevelEnum.getAmount();
            }
            has = hasLevelEnum.getLevel() >= nodeLevelEnum.getLevel();
            textList = new ArrayList<>();
            for (int i = 1; i <= nodeLevelEnum.getTextSize(); i++) {
                textList.add(MessageUtil.get(String.format("node%d.text%d", nodeLevelEnum.getLevel(), i), language));
            }
            list.add(NodeLevelVO.builder()
                    .name(MessageUtil.get(String.format("node%d.name", nodeLevelEnum.getLevel()), language))
                    .count(nodeLevelEnum.getLimitSize() - count)
                    .has(has)
                    .level(nodeLevelEnum.getLevel())
                    .amount(nodeLevelEnum.getAmount())
                    .payAmount(payAmount)
                    .text(MessageUtil.get(String.format("node%d.text", nodeLevelEnum.getLevel()), language))
                    .textList(textList)
                    .build());
        }
        //倒序
        Collections.sort(list, Collections.reverseOrder());
        return JsonResult.successResult(list);
    }

    @Override
    public void reduceAdvertHasTaskNum(Integer userId, Integer num) {
        this.baseMapper.reduceAdvertHasTaskNum(userId, num);
    }
}
