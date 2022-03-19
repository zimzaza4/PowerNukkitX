package cn.nukkit.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.Entity;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.permission.Permissible;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 能发送命令的对象。<br>
 * Who sends commands.
 *
 * <p>可以是一个玩家或者一个控制台或者一个实体或者其他。<br>
 * That can be a player or a console.</p>
 *
 * @author MagicDroidX(code) @ Nukkit Project
 * @author 粉鞋大妈(javadoc) @ Nukkit Project
 * @author smartcmd(code) @ PowerNukkitX Project
 * @see cn.nukkit.command.CommandExecutor#onCommand
 * @since Nukkit 1.0 | Nukkit API 1.0.0
 */
public interface CommandSender extends Permissible {

    /**
     * 给命令发送者返回信息。<br>
     * Sends a message to the command sender.
     *
     * @param message 要发送的信息。<br>Message to send.
     * @see cn.nukkit.utils.TextFormat
     * @since Nukkit 1.0 | Nukkit API 1.0.0
     */
    void sendMessage(String message);

    /**
     * 给命令发送者返回信息。<br>
     * Sends a message to the command sender.
     *
     * @param message 要发送的信息。<br>Message to send.
     * @since Nukkit 1.0 | Nukkit API 1.0.0
     */
    void sendMessage(TextContainer message);

    /**EntitySelector
     * 返回命令发送者所在的服务器。<br>
     * Returns the server of the command sender.
     *
     * @return 命令发送者所在的服务器。<br>the server of the command sender.
     * @since Nukkit 1.0 | Nukkit API 1.0.0
     */
    Server getServer();

    /**
     * 返回命令发送者的名称。<br>
     * Returns the name of the command sender.
     *
     * <p>如果命令发送者是一个玩家，将会返回他的玩家名字(name)不是显示名字(display name)。
     * 如果命令发送者是控制台，将会返回{@code "CONSOLE"}。<br>
     * If this command sender is a player, will return his/her player name(not display name).
     * If it is a console, will return {@code "CONSOLE"}.</p>
     * <p>当你需要判断命令的执行者是不是控制台时，可以用这个：<br>
     * When you need to determine if the sender is a console, use this:<br>
     * {@code if(sender instanceof ConsoleCommandSender) .....;}</p>
     *
     * @return 命令发送者的名称。<br>the name of the command sender.
     * @see cn.nukkit.Player#getName()
     * @see cn.nukkit.command.ConsoleCommandSender#getName()
     * @see cn.nukkit.plugin.PluginDescription
     * @since Nukkit 1.0 | Nukkit API 1.0.0
     */
    @Nonnull
    String getName();

    boolean isPlayer();

    /**
     * @return whether the sender is an entity <br>
     * please use this method to check whether the sender is an entity instead of using code {@code "xxx instanceof Entity"} <br>
     * because the sender may not an instance of {@code "Entity"} but in fact it is executing commands identity as an entity(eg: {@code "ExecutorCommandSender"})
     */
    @PowerNukkitOnly
    @Since("1.6.0.0-PNX")
    default boolean isEntity() {return false;};

    //return the entity who execute the command if the sender is a entity
    @PowerNukkitOnly
    @Since("1.6.0.0-PNX")
    @Nullable
    default Entity asEntity() {return null;};

    //return the player who execute the command if the sender is a player
    @PowerNukkitOnly
    @Since("1.6.0.0-PNX")
    @Nullable
    default Player asPlayer() {return null;};

    //return the sender's position
    @PowerNukkitOnly
    @Since("1.6.0.0-PNX")
    @Nonnull
    default Position getPosition() {return new Position(0, 0, 0,Server.getInstance().getDefaultLevel());}


    //return the sender's location
    @PowerNukkitOnly
    @Since("1.6.0.0-PNX")
    @Nonnull
    default Location getLocation() {return new Location(0, 0, 0,Server.getInstance().getDefaultLevel());}
}
