package com.fyxridd.lib.core.show.condition;

/**
 * 字符串包含
 */
public class StringHasCondition implements Condition{
    private boolean sensitive;
    private boolean has;
    private String from;
    private String to;

    public StringHasCondition() {}

    public StringHasCondition(boolean sensitive, boolean has, String from, String to) {
        this.sensitive = sensitive;
        this.has = has;
        this.from = from;
        this.to = to;
    }

    /**
     * 从条件字符串读取条件
     * @param condition 条件字符串
     * @return 条件,异常返回null
     */
    @Override
    public boolean loadFromString(String condition) {
        //sensitive
        boolean sensitive;
        if (condition.charAt(0) == 'y') sensitive = true;
        else if (condition.charAt(0) == 'n') sensitive = false;
        else return false;
        //has
        int pos1 = 1;
        int pos2 = condition.indexOf(39, pos1+1);
        int pos3 = pos2+4;
        int pos4 = condition.length()-1;
        String check = condition.substring(pos2+1, pos3);
        boolean has;
        if (check.equalsIgnoreCase("has")) has = true;
        else if (check.equalsIgnoreCase("not")) has = false;
        else return false;
        //from,to
        String from;
        if (pos2-pos1 < 1) return false;
        if (pos2-pos1 == 1) from = "";
        else from = condition.substring(pos1 + 1, pos2);
        String to;
        if (pos4-pos3 < 1) return false;
        if (pos4-pos3 == 1) to = "";
        else to = condition.substring(pos3 + 1, pos4);
        //设置
        this.sensitive = sensitive;
        this.has = has;
        this.from = from;
        this.to = to;
        return true;
    }

    @Override
    public String saveToString() {
        String sensitive = this.sensitive?"y":"n";
        String has = this.has?"has":"not";
        return sensitive+"'"+from+"'"+has+"'"+to+"'";
    }

    @Override
    public boolean check() {
        String from,to;
        if (!sensitive) {
            from = this.from.toLowerCase();
            to = this.to.toLowerCase();
        } else {
            from = this.from;
            to = this.to;
        }
        if (has) return from.contains(to);
        else return !from.contains(to);
    }

    @Override
    public StringHasCondition clone(){
        return new StringHasCondition(sensitive, has, from, to);
    }

    @Override
    public void replace(String name, String value) {
        from = from.replace(name, value);
        to = to.replace(name, value);
    }

    @Override
    public boolean hasFix() {
        return (" " + from + " ").split("\\{[A-Za-z0-9_]+\\}").length > 1 ||
                (" " + to + " ").split("\\{[A-Za-z0-9_]+\\}").length > 1;
    }

    @Override
    public String toString() {
        if (has) return from+" has "+to;
        return from+" not "+to;
    }
}
