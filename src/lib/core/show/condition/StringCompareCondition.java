package lib.core.show.condition;

/**
 * 字符串比较
 */
public class StringCompareCondition implements Condition{
    private boolean sensitive;
    //'=='
    //'!='
    private String operate;
    private String from;
    private String to;

    public StringCompareCondition() {}

    public StringCompareCondition(boolean sensitive, String operate, String from, String to) {
        this.sensitive = sensitive;
        this.operate = operate;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean loadFromString(String condition) {
        //sensitive
        boolean sensitive;
        if (condition.charAt(0) == 'y') sensitive = true;
        else if (condition.charAt(0) == 'n') sensitive = false;
        else return false;
        //operate
        int pos1 = 1;
        int pos2 = condition.indexOf(39, pos1+1);
        int pos3 = pos2+3;
        int pos4 = condition.length()-1;
        String check = condition.substring(pos2+1, pos3);
        String operate;
        if (check.equalsIgnoreCase("==")) operate = "==";
        else if (check.equalsIgnoreCase("!=")) operate = "!=";
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
        this.operate = operate;
        this.from = from;
        this.to = to;
        return true;
    }

    @Override
    public String saveToString() {
        String sensitive = this.sensitive?"y":"n";
        return sensitive+"'"+from+"'"+operate+"'"+to+"'";
    }

    @Override
    public boolean check() {
        String from,to;
        if (!sensitive) {
            from = this.from.toLowerCase();
            to = this.to.toLowerCase();
        }else {
            from = this.from;
            to = this.to;
        }
        if (operate.equalsIgnoreCase("==")) {
            return from.equals(to);
        }else {
            return !from.equals(to);
        }
    }

    @Override
    public StringCompareCondition clone(){
        return new StringCompareCondition(sensitive, operate, from, to);
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
        return from+" "+operate+" "+to;
    }
}
