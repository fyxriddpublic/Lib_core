package lib.core.show.condition;

import lib.core.api.CoreApi;

/**
 * 表达式<br>
 * 有限制,不支持负数操作,比如'-3+4'可以改为'0-3+4'
 */
public class Exp implements Cloneable{
    /**
     * 字符串表达式
     */
    private String exp;

    public Exp(String exp) {
        this.exp = exp;
    }

    /**
     * 表达式from 比较符operate 表达式to
     * @param from 前面的表达式,不为null
     * @param to 后面的表达式,不为null
     * @param operate 比较符,不为null
     * @return 比较结果
     */
    public static boolean compare(Exp from, Exp to, Operate operate) {
        String fromExp = from.getExp();
        String toExp = to.getExp();
        int fromResult = CoreApi.calc(fromExp);
        int toResult = CoreApi.calc(toExp);
        return operate.compare(fromResult, toResult);
    }

    public String getExp() {
        return exp;
    }

    @Override
    public Exp clone() {
        return new Exp(exp);
    }

    public void replace(String name, String value) {
        exp = exp.replace(name, value);
    }

    public boolean hasFix() {
        return (" " + exp + " ").split("\\{[A-Za-z0-9_]+\\}").length > 1;
    }

    @Override
    public String toString() {
        return exp;
    }
}
