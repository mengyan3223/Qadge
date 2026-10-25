package me.lengyu.qedge.coldrain;

import me.lengyu.qedge.plugin.bean.MsgData;
/**
 * @Author 梦魇
 * @Description 梦魇Java接口
 */

public interface ColdRainFeature {
    boolean shouldHandle(MsgData msgData);
    void handle(MsgData msgData, ColdRainCore core);
}
