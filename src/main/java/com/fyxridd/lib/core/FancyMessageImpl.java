package com.fyxridd.lib.core;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.fyxridd.lib.core.api.hashList.HashList;
import com.fyxridd.lib.core.api.hashList.HashListImpl;
import com.fyxridd.lib.core.show.condition.Condition;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.api.MessageApi;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.JSONException;
import org.json.JSONStringer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FancyMessageImpl implements FancyMessage {
	private static ProtocolManager pm;
    public static void init(ProtocolManager pm) {
		FancyMessageImpl.pm = pm;
	}
	
	private final List<MessagePart> messageParts;

	private FancyMessageImpl(List<MessagePart> messageParts) {
		this.messageParts = messageParts;
	}
	
	public FancyMessageImpl(final String firstPartText) {
		messageParts = new ArrayList<>();
		messageParts.add(new MessagePart(firstPartText));
	}

    public void combine(FancyMessage fm, boolean front) {
        int size = fm.getMessageParts().size();
        if (front) {
            for (int i=size-1;i>=0;i--) {
                MessagePart mp = fm.getMessageParts().get(i).clone();
                this.messageParts.add(0, mp);
            }
        }else {
            for (int i=0;i<size;i++) {
                MessagePart mp = fm.getMessageParts().get(i).clone();
                this.messageParts.add(mp);
            }
        }
    }

    public void fix() {
        int index = 0;
        while (true) {
            if (index >= messageParts.size()) break;
            MessagePart mpFrom = messageParts.get(index);
            //检测删除空的MessagePart
            if (mpFrom.isEmpty()) messageParts.remove(index);
            else {
                if (index >= messageParts.size()-1) break;
                //对每个MessagePart与其后的MessagePart进行合并检测
                MessagePart mpTo = messageParts.get(index + 1);
                if (mpFrom.isSame(mpTo)) {
                    mpFrom.combine(mpTo);
                    messageParts.remove(index + 1);
                } else index++;
            }
        }
    }

	@Override
	public String toString() {
		return getText();
	}

    @Override
	public FancyMessageImpl text(final String text) {
		latest().text = text;
		return this;
	}

    public FancyMessageImpl con(final List<Condition> con) {
        latest().con = con;
        return this;
    }

    public FancyMessageImpl func(final String funcName) {
		latest().func = funcName;
		return this;
	}

	public FancyMessageImpl data(final String data) {
		latest().data = data;
		return this;
	}
	
	public FancyMessageImpl color(final ChatColor color) {
		if (!color.isColor()) {
			throw new IllegalArgumentException(color.name() + " is not a color");
		}
		latest().color = color;
		return this;
	}

    public FancyMessageImpl item(String item) {
        latest().item = item;
        return this;
    }

	public FancyMessageImpl style(final ChatColor... styles) {
		for (final ChatColor style : styles) {
			if (!style.isFormat()) {
				throw new IllegalArgumentException(style.name() + " is not a style");
			}
		}
		latest().styles = styles;
		return this;
	}

	public FancyMessageImpl file(final String path) {
		onClick("open_file", path);
		return this;
	}

	public FancyMessageImpl link(final String url) {
		onClick("open_url", url);
		return this;
	}

	public FancyMessageImpl suggest(final String command) {
		onClick("suggest_command", command);
		return this;
	}

	public FancyMessageImpl command(final String command) {
		onClick("run_command", command);
		return this;
	}

	public FancyMessageImpl itemTooltip(final String itemJSON, final String hoverActionString) {
		onHover("show_item", itemJSON, hoverActionString);
		return this;
	}

	public FancyMessageImpl itemTooltip(final ItemStack itemStack, final String hoverActionString) {
		return itemTooltip(MessageApi.getHoverActionData(itemStack), hoverActionString);
	}

	public FancyMessageImpl tooltip(final String text, final String hoverActionString) {
		final String[] lines = text.split("\\n");
		if (lines.length <= 1) {
			onHover("show_text", text, hoverActionString);
		} else {
			itemTooltip(makeMultilineTooltip(lines), hoverActionString);
		}
		return this;
	}

	public FancyMessageImpl then(final Object obj) {
		messageParts.add(new MessagePart(obj.toString()));
		return this;
	}
	
	public String toJSONString() {
		final JSONStringer json = new JSONStringer();
		try {
			if (messageParts.size() == 1) {
				latest().writeJson(json);
			} else {
				json.object().key("text").value("").key("extra").array();
				for (final MessagePart part : messageParts) part.writeJson(json);
				json.endArray().endObject();
			}
		} catch (final JSONException e) {
			throw new RuntimeException("invalid message");
		}
		return json.toString();
	}
	
	public void send(Player p, boolean check){
		try {
			PacketContainer pc = new PacketContainer(PacketType.Play.Server.CHAT);
			FancyMessageImpl copy = clone();
			for (MessagePart mp:copy.getMessageParts()) FuncManager.update(mp, p.getName());
			pc.getChatComponents().write(0, WrappedChatComponent.fromJson(copy.toJSONString()));
			pm.sendServerPacket(p, pc, check);
		} catch (InvocationTargetException e) {
		}
	}

	public String getText() {
		String result = "";
        for (MessagePart mp : messageParts) result += mp.text;
		return result;
	}
	
	public List<MessagePart> getMessageParts() {
		return messageParts;
	}

	public MessagePart latest() {
		return messageParts.get(messageParts.size() - 1);
	}

	public void update() {
        for (MessagePart mp: messageParts) {
            //hasFix
            if (!mp.hasFix) {
                mp.hasFix = (" " + mp.text + mp.item+mp.func+mp.data+mp.clickActionData+mp.hoverActionData+" ")
                        .split("\\{[A-Za-z0-9_]+\\}").length > 1;

                if (!mp.hasFix && mp.con != null) {
                    for (Condition condition : mp.con) {
                        if (condition.hasFix()) {
                            mp.hasFix = true;
                            break;
                        }
                    }
                }
            }
            //listFix
            HashList<String> listFix = new HashListImpl<>();
            if (mp.con != null) {
                for (Condition condition: mp.con) checkAdd(listFix, condition.toString());
            }
            checkAdd(listFix, mp.text);
            checkAdd(listFix, mp.func);
            checkAdd(listFix, mp.data);
            checkAdd(listFix, mp.item);
            checkAdd(listFix, mp.clickActionData);
            checkAdd(listFix, mp.hoverActionString);
            mp.listFix = listFix;
        }
	}

    /**
     * 检测添加列表Fix
     * @param listFix 列表Fix,不为null
     * @param s 检测的字符串,可能包含{数字.属性}或{数字.方法名()},可为null
     */
    public void checkAdd(HashList<String> listFix, String s) {
        if (s == null) return;
        boolean start = false;
        int startIndex = 0;
        char[] cc = s.toCharArray();
        for (int i=0;i<cc.length;i++) {
            char c = cc[i];
            if (c == '{') {
                start = true;
                startIndex = i;
            }else if (c == '}') {
                if (start) {
                    int endIndex = i;
                    if (endIndex - startIndex > 2) {//合格内容长度必须>=2
                        String check = s.substring(startIndex + 1, endIndex);//check为'数字.属性'或'数字.方法名()'或'数字.方法名(name)'
                        int index = check.indexOf('.');
                        if (index != -1) {//包含.
                            String[] ss = check.split("\\.");
                            if (ss.length == 2) {
                                try {
                                    Integer.parseInt(ss[0]);
                                } catch (NumberFormatException e) {
                                    continue;
                                }
                                listFix.add(check);
                            }
                        }
                    }
                    //重置
                    start = false;
                    startIndex = 0;
                }
            }
        }
    }

	@Override
	public FancyMessageImpl clone() {
		List<MessagePart> messageParts = new ArrayList<>();
		for (MessagePart mp:this.messageParts) {
			messageParts.add(mp.clone());
		}
		FancyMessageImpl result = new FancyMessageImpl(messageParts);
		return result;
	}

	public void checkCondition() {
		Iterator<MessagePart> it = messageParts.iterator();
		while (it.hasNext()) {
			MessagePart mp = it.next();
			if (mp.con != null) {
				//检测隐藏显示
				for (Condition condition:mp.con) {
					//检测失败,删除MessagePart
					if (!condition.check()) {
						it.remove();
						break;
					}
				}
			}
		}
	}

    private String makeMultilineTooltip(final String[] lines) {
		final JSONStringer json = new JSONStringer();
		try {
			json.object().key("id").value(1);
			json.key("tag").object().key("display").object();
			json.key("Name").value("\\u00A7f" + lines[0].replace("\"", "\\\""));
			json.key("Lore").array();
			for (int i = 1; i < lines.length; i++) {
				final String line = lines[i];
				json.value(line.isEmpty() ? " " : line.replace("\"", "\\\""));
			}
			json.endArray().endObject().endObject().endObject();
		} catch (final JSONException e) {
			throw new RuntimeException("invalid tooltip");
		}
		return json.toString();
	}
	
	private void onClick(final String name, final String data) {
		final MessagePart latest = latest();
		latest.clickActionName = name;
		latest.clickActionData = data;
	}

	private void onHover(final String name, final String data, final String hoverActionString) {
		final MessagePart latest = latest();
		latest.hoverActionName = name;
		latest.hoverActionData = data;
        latest.hoverActionString = hoverActionString;
	}
}
