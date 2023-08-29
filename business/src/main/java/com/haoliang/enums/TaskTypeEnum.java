package com.haoliang.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    CONCERN(1, "关注"),
    COMMENTS(2, "评论"),
    LIKE_TASK(3, "点赞"),
    IMG_ADVERT(4, "图片广告"),
    VIDEO_ADVERT(5, "视频广告"),
    ;

    private int type;

    private String name;

    /**
     * 旧版接口任务类型
     * @return
     */
    public static List<Integer> getOldTypeList() {
        return Arrays.asList(TaskTypeEnum.CONCERN.type, TaskTypeEnum.COMMENTS.type, TaskTypeEnum.LIKE_TASK.type);
    }

    /**
     * 新版任务类型
     */
    public static List<Integer> getNewTypeList() {
        return Arrays.asList(TaskTypeEnum.CONCERN.type, TaskTypeEnum.IMG_ADVERT.type, VIDEO_ADVERT.type);
    }

    public static TaskTypeEnum valueOf(Integer value) {
        for (TaskTypeEnum taskTypeEnum : values()) {
            if (taskTypeEnum.getType()==value) {
                return taskTypeEnum;
            }
        }
        return null;
    }
}
