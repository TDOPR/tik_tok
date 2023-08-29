package com.haoliang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haoliang.common.model.JsonResult;
import com.haoliang.common.model.PageParam;
import com.haoliang.common.model.dto.PageDTO;
import com.haoliang.common.model.dto.TypeDTO;
import com.haoliang.common.model.vo.PageVO;
import com.haoliang.model.TiktokTask;
import com.haoliang.model.condition.TiktokTaskCondition;
import com.haoliang.model.dto.BuyPackageDTO;
import com.haoliang.model.dto.ForceDeleteDTO;
import com.haoliang.model.dto.PublishConcernTiktokTaskDTO;
import com.haoliang.model.dto.PublishTiktokTask;
import com.haoliang.model.vo.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TiktokTaskService  extends IService<TiktokTask> {

    JsonResult<PageVO<MyTiktokTaskVO>> getMyPublishList(PageDTO pageDTO);

    JsonResult getPricesAndBalance();

    JsonResult buyPackage(Integer id);

    JsonResult<TaskNumVO> getCountPackageBalance();

    JsonResult pagelist(PageParam<TiktokTask, TiktokTaskCondition> pageParam);

    JsonResult addOrEdit(Long id,Integer type, Integer channel,String desc,Integer num, MultipartFile video, MultipartFile img, String username,String tiktokUserId);

    JsonResult deleteByIdList(ForceDeleteDTO forceDeleteDTO);

    JsonResult saveAndPublishAdvert(Integer type, Integer channel, String desc, Integer num, MultipartFile video, MultipartFile img);

    JsonResult<AdvertTaskResultVO> getAdvertList();

    JsonResult<PublishTaskVO> getPublishList(TypeDTO pageDTO, List<Integer> typeList);


    JsonResult publish(PublishTiktokTask publishTiktokTask);

    JsonResult publishV2(PublishConcernTiktokTaskDTO publishConcernTiktokTaskDTO);

    JsonResult<PageVO<MyTiktokTaskVO>> getMyPublishListV2(PageDTO pageDTO);

    JsonResult newBuyPackage(BuyPackageDTO buyPackageDTO);

    JsonResult getPricesAndBalanceV2(Integer type);

    JsonResult<TaskNumVO> getCountPackageBalanceV2(Integer type);
}
