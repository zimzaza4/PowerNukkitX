package cn.nukkit.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.utils.CommandParser;
import cn.nukkit.utils.CommandSyntaxException;
import cn.nukkit.utils.TextFormat;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlaySoundCommand extends VanillaCommand {

    public PlaySoundCommand(String name) {
        super("playsound", "Plays a sound.", "/playsound <sound: string> [player: target] [position: x y z] [volume: float] [pitch: float] [minimumVolume: float]");
        this.setPermission("nukkit.command.playsound");
        this.getCommandParameters().clear();
        this.addCommandParameters("default", new CommandParameter[]{
                CommandParameter.newEnum("sound",false, Arrays.stream(Sound.values()).map(s -> s.getSound()).collect(Collectors.toList()).toArray(new String[0])),
                CommandParameter.newType("player",true, CommandParamType.TARGET),
                CommandParameter.newType("position",true, CommandParamType.POSITION),
                CommandParameter.newType("volume",true, CommandParamType.FLOAT),
                CommandParameter.newType("pitch",true, CommandParamType.FLOAT),
                CommandParameter.newType("minimumVolume",true, CommandParamType.FLOAT)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        CommandParser parser = new CommandParser(this, sender, args);
        try {
            String sound = parser.parseString();
            List<Player> targets;
            Position position = null;
            double volume = 1;
            double pitch = 1;
            double minimumVolume = 0;

            if (args.length > 1) {
                targets = parser.parseTargetPlayers();
                if (args.length > 2) {
                    position = parser.parsePosition();
                    if (args.length > 5) {
                        volume = parser.parseDouble();
                        if (args.length > 6) {
                            pitch = parser.parseDouble();
                            if (args.length > 7) {
                                minimumVolume = Math.max(parser.parseDouble(), 0);
                            }
                        }
                    }
                }
            } else if (sender.isPlayer()) {
                targets = Lists.newArrayList(sender.asPlayer());
            } else {
                sender.sendMessage(TextFormat.RED + "No targets matched selector");
                return false;
            }

            position = sender.getPosition();

            if (targets.size() == 0) {
                sender.sendMessage(TextFormat.RED + "No targets matched selector");
                return false;
            }

            double maxDistance = volume > 1 ? volume * 16 : 16;
            List<String> successes = Lists.newArrayList();

            for (Player player : targets) {
                String name = player.getName();
                PlaySoundPacket packet = new PlaySoundPacket();

                if (position.distance(player) > maxDistance) {
                    if (minimumVolume <= 0) {
                        sender.sendMessage(String.format(TextFormat.RED + "Player %1$s is too far away to hear the sound", name));
                        continue;
                    }

                    packet.volume = (float) minimumVolume;
                    packet.x = player.getFloorX();
                    packet.y = player.getFloorY();
                    packet.z = player.getFloorZ();
                } else {
                    packet.volume = (float) volume;
                    packet.x = position.getFloorX();
                    packet.y = position.getFloorY();
                    packet.z = position.getFloorZ();
                }

                packet.name = sound;
                packet.pitch = (float) pitch;
                player.dataPacket(packet);

                successes.add(name);
            }

            sender.sendMessage(String.format("Played sound '%1$s' to %2$s", sound, String.join(", ", successes)));
        } catch (CommandSyntaxException e) {
            sender.sendMessage(parser.getErrorMessage());
            return false;
        }

        return true;
    }
}
