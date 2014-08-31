package pw.ollie.minerefer.command;

import pw.ollie.minerefer.MineRefer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.*;

public class ReferCommandExecutor implements CommandExecutor {
    private final MineRefer plugin;

    public ReferCommandExecutor(MineRefer plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String lbl, String[] args) {
        // unnecessary sanity check
        if (!cmd.getName().equalsIgnoreCase("referrer")) {
            return false;
        }
        if (!(cs instanceof Player)) {
            message(cs, RED + "Only players can submit referrals!");
            return true;
        }
        if (args.length != 1) {
            message(cs, RED + "Usage: /referrer <player>");
            return true;
        }

        Player sender = (Player) cs;
        if (plugin.hasReferred(sender.getUniqueId())) {
            message(cs, RED + "You can't be referred by multiple people!");
            return true;
        }

        String referrer = args[0];
        Player player = plugin.getServer().getPlayer(referrer);
        if (player == null) {
            message(cs, RED + "That player isn't online!");
            return true;
        }

        plugin.giveRewards(player);
        message(cs, GRAY + player.getName() + " was rewarded for referring you!");
        message(player, GRAY + "You've been rewarded for referring " + cs.getName());
        return true;
    }

    private void message(CommandSender cs, String... messages) {
        if (messages == null || messages.length < 1) {
            return;
        }

        for (String message : messages) {
            cs.sendMessage(message);
        }
    }
}
