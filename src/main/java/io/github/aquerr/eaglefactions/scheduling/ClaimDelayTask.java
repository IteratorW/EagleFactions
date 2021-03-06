package io.github.aquerr.eaglefactions.scheduling;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Claim;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.function.Consumer;

public class ClaimDelayTask implements EagleFactionsConsumerTask<Task>
{
    private final FactionLogic factionLogic = EagleFactions.getPlugin().getFactionLogic();
    private final Player player;
    private final Vector3i chunkPosition;
    private final int claimDelay;
    private final boolean shouldClaimByItems;

    private int currentWaitSeconds = 0;

    public ClaimDelayTask(Player player, Vector3i chunkPosition)
    {
        this.player = player;
        this.chunkPosition = chunkPosition;
        this.claimDelay = EagleFactions.getPlugin().getConfiguration().getConfigFields().getClaimDelay();
        this.shouldClaimByItems = EagleFactions.getPlugin().getConfiguration().getConfigFields().shouldClaimByItems();
    }

    @Override
    public void accept(Task task)
    {
        if(!chunkPosition.equals(player.getLocation().getChunkPosition()))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MOVED_FROM_THE_CHUNK));
            task.cancel();
        }

        if(currentWaitSeconds >= claimDelay)
        {
            final Optional<Faction> optionalFaction = this.factionLogic.getFactionByPlayerUUID(player.getUniqueId());
            if(!optionalFaction.isPresent())
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                task.cancel();
            }

            if(shouldClaimByItems)
            {
                boolean didSucceed = this.factionLogic.addClaimByItems(player, optionalFaction.get(), player.getWorld().getUniqueId(), chunkPosition);
                if(didSucceed)
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunkPosition.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                else
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
            }
            else
            {
                factionLogic.addClaim(optionalFaction.get(), new Claim(player.getWorld().getUniqueId(), chunkPosition));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunkPosition.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
            }
            task.cancel();
        }
        else
        {
            player.sendMessage(ChatTypes.ACTION_BAR, Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RESET, currentWaitSeconds));
            currentWaitSeconds++;
        }
    }
}
