package lib.core.show.condition;

/**
 * 比较符
 */
public enum Operate{
    gt(">"),
    lt("<"),
    ge(">="),
    le("<="),
    eq("=="),
    ne("!=");

    private String show;

    private Operate(String show) {
        this.show = show;
    }

    /**
     * 从字符串中读取比较符
     * @param operate 比较符字符串
     * @return 比较符
     */
    public static Operate fromString(String operate) {
        if (operate.equalsIgnoreCase(">")) return Operate.gt;
        else if (operate.equalsIgnoreCase("<")) return Operate.lt;
        else if (operate.equalsIgnoreCase(">=")) return Operate.ge;
        else if (operate.equalsIgnoreCase("<=")) return Operate.le;
        else if (operate.equalsIgnoreCase("==")) return Operate.eq;
        else return ne;
    }

    /**
     * 比较两个值
     * @param fromResult 前一个值
     * @param toResult 后一个值
     * @return 比较结果
     */
    public boolean compare(int fromResult, int toResult) {
        switch (this) {
            case gt:
                return fromResult > toResult;
            case lt:
                return fromResult < toResult;
            case ge:
                return fromResult >= toResult;
            case le:
                return fromResult <= toResult;
            case eq:
                return fromResult == toResult;
            case ne:
                return fromResult != toResult;
        }
        //不会发生的异常
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return show;
    }
}