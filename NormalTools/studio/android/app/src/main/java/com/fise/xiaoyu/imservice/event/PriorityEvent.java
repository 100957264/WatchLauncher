package com.fise.xiaoyu.imservice.event;

/**
 * 以下是EventBus 2.x 版本的注释，现已升级<br/>
 *
 * Priority 事件
 * 临时解决
 * 背景: 1.EventBus的cancelEventDelivery的只能在postThread中运行，而且没有办法绕过这一点
 * 2. onEvent(A a)  onEventMainThread(A a) 这个两个是没有办法共存的
 * 解决: 抽离出那些需要优先级的event，在onEvent通过handler调用主线程，
 * 然后cancelEventDelivery
 */
public class PriorityEvent {

    public Object object;
    public Event event;

    public enum  Event{
        MSG_RECEIVED_MESSAGE,
        REQ_FRIENDS_MESSAGE,
        REQ_RECEIVED_MESSAGE,
        WEI_FRIENDS_MESSAGE,
        MSG_DEV_MESSAGE,
        MSG_VEDIO_MESSAGE,
        MSG_VEDIO_MESSAGE_TEST,//测试
        MSG_VEDIO_ONLINE_DEV,
        MSG_VEDIO_ONLINE_DEV_START
    }
}
