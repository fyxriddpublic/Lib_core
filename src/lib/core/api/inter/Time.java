package lib.core.api.inter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Time {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    private int hour;
    private int minute;
    private Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    /**
     * 检测时间是否达到(24小时制)
     * @return 是否达到
     */
    public boolean hasReach() {
        Calendar now = Calendar.getInstance();

        Calendar tar = Calendar.getInstance();
        tar.set(Calendar.HOUR_OF_DAY, hour);
        tar.set(Calendar.MINUTE, minute);
        tar.set(Calendar.SECOND, 0);

        return now.after(tar);
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public long getTimeInMills() {
        return getCalendar().getTimeInMillis();
    }

    public Calendar getCalendar() {
        Calendar result = Calendar.getInstance();
        result.set(Calendar.HOUR_OF_DAY, hour);
        result.set(Calendar.MINUTE, minute);
        result.set(Calendar.SECOND, 0);
        return result;
    }

    /**
     * 获取时间显示,格式'HH:mm'
     * @return 时间显示
     */
    public String getTimeShow() {
        return sdf.format(new Date(getTimeInMills()));
    }

    /**
     * 从字符串中读取时间
     * @param data 时间保存字符串,格式'时:分'
     * @return 时间,异常返回null
     */
    public static Time load(String data) {
        if (data == null) return null;

        String[] args = data.split(":");
        if (args.length == 2) {
            try {
                int hour = Integer.parseInt(args[0]);
                int minute = Integer.parseInt(args[1]);
                return new Time(hour, minute);
            } catch (Exception e) {
                //do nothing
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return hour+":"+minute;
    }
}