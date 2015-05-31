package lib.core.api.adaptor;

import lib.core.api.inter.FunctionInterface;
import org.bukkit.entity.Player;

public class FunctionAdaptor implements FunctionInterface{
    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean isOn(String name, String subFunc) {
        return false;
    }

    @Override
    public void setOn(String name, String subFunc, boolean on) {
    }

    @Override
    public void onOperate(Player p, String data) {
    }
}
