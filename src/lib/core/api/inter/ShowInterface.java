package lib.core.api.inter;

/**
 * 显示接口<br>
 * 所有调用ShowManager.show(xxx)方法的类必须实现这个接口<br>
 * 用来页面跳转(刷新)时回调
 */
public interface ShowInterface {
    /**
     * 重新加载页面(刷新)
     * @param playerContext 玩家页面上下文
     */
    public void show(PlayerContext playerContext);
}
