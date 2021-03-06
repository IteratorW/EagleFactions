package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.entities.Claim;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AttackLogic
{
    private static AttackLogic INSTANCE = null;

    private final ConfigFields _configFields;
    private final FactionLogic _factionLogic;

    public AttackLogic(EagleFactions eagleFactions)
    {
        INSTANCE = this;
        _configFields = eagleFactions.getConfiguration().getConfigFields();
        _factionLogic = eagleFactions.getFactionLogic();
    }

    public static AttackLogic getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new AttackLogic(eagleFactions);
        else return INSTANCE;
    }

    public void attack(Player player, Vector3i attackedChunk)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(new Consumer<Task>()
        {
            int seconds = 1;

            @Override
            public void accept(Task task)
            {
                if(attackedChunk.toString().equals(player.getLocation().getChunkPosition().toString()))
                {
                    if(seconds == _configFields.getAttackTime())
                    {
                        //Because it is not possible to attack territory that is not claimed then we can safely get faction here.
                        Faction chunkFaction = _factionLogic.getFactionByChunk(player.getWorld().getUniqueId(), attackedChunk).get();

                        informAboutDestroying(chunkFaction);
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.CLAIM_DESTROYED));

                        _factionLogic.removeClaim(chunkFaction, new Claim(player.getWorld().getUniqueId(), attackedChunk));
                        task.cancel();
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RESET, seconds));
                        seconds++;
                    }
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MOVED_FROM_THE_CHUNK));
                    task.cancel();
                }
            }
        }).submit(EagleFactions.getPlugin());
    }

    public void blockClaiming(String factionName)
    {
        if(EagleFactions.ATTACKED_FACTIONS.containsKey(factionName))
        {
            EagleFactions.ATTACKED_FACTIONS.replace(factionName, 120);
        }
        else
        {
            EagleFactions.ATTACKED_FACTIONS.put(factionName, 120);
            runClaimingRestorer(factionName);
        }
    }

    public void runClaimingRestorer(String factionName)
    {

        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(new Consumer<Task>()
        {
            @Override
            public void accept(Task task)
            {

                if(EagleFactions.ATTACKED_FACTIONS.containsKey(factionName))
                {
                    int seconds = EagleFactions.ATTACKED_FACTIONS.get(factionName);

                    if (seconds <= 0)
                    {
                        EagleFactions.ATTACKED_FACTIONS.remove(factionName);
                        task.cancel();
                    }
                    else
                    {
                        EagleFactions.ATTACKED_FACTIONS.replace(factionName, seconds, seconds - 1);
                    }
                }
            }
        }).submit(EagleFactions.getPlugin());
    }

    public void informAboutAttack(Faction faction)
    {
        List<Player> playersList = _factionLogic.getOnlinePlayers(faction);

        playersList.forEach(x -> x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_FACTION_IS_UNDER + " ", TextColors.RED, PluginMessages.ATTACK, TextColors.RESET, "!")));
    }

    public void informAboutDestroying(Faction faction)
    {
        List<Player> playersList = _factionLogic.getOnlinePlayers(faction);

        playersList.forEach(x -> x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.ONE_OF_YOUR_CLAIMS_HAS_BEEN + " ", TextColors.RED, PluginMessages.DESTROYED, TextColors.RESET, " " + PluginMessages.BY_AN_ENEMY)));
    }

    public void blockHome(UUID playerUUID)
    {
        if(EagleFactions.BLOCKED_HOME.containsKey(playerUUID))
        {
            EagleFactions.BLOCKED_HOME.replace(playerUUID, _configFields.getHomeBlockTimeAfterDeathInOwnFaction());
        }
        else
        {
            EagleFactions.BLOCKED_HOME.put(playerUUID, _configFields.getHomeBlockTimeAfterDeathInOwnFaction());
            runHomeUsageRestorer(playerUUID);
        }
    }

    public void runHomeUsageRestorer(UUID playerUUID)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(new Consumer<Task>()
        {
            @Override
            public void accept(Task task)
            {
                if (EagleFactions.BLOCKED_HOME.containsKey(playerUUID))
                {
                    int seconds = EagleFactions.BLOCKED_HOME.get(playerUUID);

                    if (seconds <= 0)
                    {
                        EagleFactions.BLOCKED_HOME.remove(playerUUID);
                        task.cancel();
                    }
                    else
                    {
                        EagleFactions.BLOCKED_HOME.replace(playerUUID, seconds, seconds - 1);
                    }
                }
            }
        }).submit(EagleFactions.getPlugin());
    }

}
