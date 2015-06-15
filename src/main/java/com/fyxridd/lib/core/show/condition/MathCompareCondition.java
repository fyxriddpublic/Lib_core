package com.fyxridd.lib.core.show.condition;

/**
 * 数学比较
 */
public class MathCompareCondition implements Condition{
    private Exp from;
    private Exp to;
    private Operate operate;

    public MathCompareCondition(){}

    /**
     * @param from 不为null
     * @param to 不为null
     * @param operate 不为null
     */
    public MathCompareCondition(Exp from, Exp to, Operate operate) {
        this.from = from;
        this.to = to;
        this.operate = operate;
    }

    @Override
    public boolean loadFromString(String condition) {
        if (condition == null) return false;
        String[] ss = condition.split(" ");
        if (ss.length != 3) return false;
        Operate operate = Operate.fromString(ss[1]);
        if (operate == null) return false;
        Exp from = new Exp(ss[0]);
        Exp to = new Exp(ss[2]);
        this.from = from;
        this.to = to;
        this.operate = operate;
        return true;
    }

    @Override
    public String saveToString() {
        return from.toString()+" "+operate.toString()+" "+to.toString();
    }

    @Override
    public boolean check() {
        return Exp.compare(from, to, operate);
    }

    @Override
    public MathCompareCondition clone(){
        return new MathCompareCondition(from.clone(), to.clone(), Operate.valueOf(operate.name()));
    }

    @Override
    public void replace(String name, String value) {
        from.replace(name, value);
        to.replace(name, value);
    }

    @Override
    public boolean hasFix() {
        return from.hasFix() || to.hasFix();
    }

    @Override
    public String toString() {
        return from.toString()+" "+operate.toString()+" "+to.toString();
    }
}
