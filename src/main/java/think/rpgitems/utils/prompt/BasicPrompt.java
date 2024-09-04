package think.rpgitems.utils.prompt;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicPrompt implements IPrompt {
    private final Player player;
    private final List<String> args;
    @Getter
    private final List<String> result = new ArrayList<>();

    public BasicPrompt(Player player, String... args) {
        this(player, Lists.newArrayList(args));
    }

    public BasicPrompt(Player player, List<String> args) {
        this.player = player;
        this.args = args;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public String getCancelKey() {
        return "cancel";
    }

    public void send(String msg) {
        PromptManager.send(player, msg.replace("%cancel%", this.getCancelKey()));
    }

    @Override
    public boolean putPromptResult(String result) {
        this.result.add(result);
        if (this.result.size() < args.size()) {
            send(args.get(this.result.size()));
            return false;
        }
        return true;
    }

    @Override
    public void startPrompt() {
        send(args.get(this.result.size()));
    }

    @Override
    public void cancelPrompt() {
        player.sendMessage("§c已取消补全");
    }

}
