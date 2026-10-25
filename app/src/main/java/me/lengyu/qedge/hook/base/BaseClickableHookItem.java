package me.lengyu.qedge.hook.base;

/**
 * @Author 梦魇
 * @Description 基础可点击钩子项
 */
public abstract class BaseClickableHookItem<T> extends BaseSwitchHookItem {

    public abstract void ConfigContent(Runnable onDismiss);
}