package lib.core.show.condition;

/**
 * 条件接口
 */
public interface Condition extends Cloneable{
    /**
     * 从条件字符串读取条件
     * @param condition 条件字符串
     * @return 读取是否成功
     */
    public boolean loadFromString(String condition);

    /**
     * 保存条件为字符串<br>
     * 保存后可以用loadFromString读取
     * @return 保存后的字符串
     */
    public String saveToString();

    /**
     * 检测条件是否满足
     * @return 是否满足
     */
    public boolean check();

    /**
     * 替换
     * @param name 名
     * @param value 值
     */
    public void replace(String name, String value);

    /**
     * 是否包含替换符
     * @return 是否包含
     */
    public boolean hasFix();

    public Condition clone();
}
