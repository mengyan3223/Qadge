package me.lengyu.qedge.hook.annotation;

/**
 * @Author 梦魇
 * @Description 钩子分类
 */
@HookItemAnnotation(value = "钩子分类", category = "item")
public interface HookCategory {
    String CHAT = "chat";
    String API = "api";
    String OTHER = "other";
    String ITEM = "item";
}