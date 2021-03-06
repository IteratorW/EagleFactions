package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class InviteCommand extends AbstractCommand
{
    public InviteCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalInvitedPlayer = context.<Player>getOne("player");

        if(optionalInvitedPlayer.isPresent())
        {
            if(source instanceof Player)
            {
                Player senderPlayer = (Player)source;
                Player invitedPlayer = optionalInvitedPlayer.get();
                Optional<Faction> optionalSenderFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(senderPlayer.getUniqueId());

                if(optionalSenderFaction.isPresent())
                {
                    Faction senderFaction = optionalSenderFaction.get();

                    if (this.getPlugin().getFlagManager().canInvite(senderPlayer.getUniqueId(), senderFaction))
                    {
                        if(getPlugin().getConfiguration().getConfigFields().isPlayerLimit())
                        {
                            int playerCount = 0;
                            playerCount += senderFaction.getLeader().toString().equals("") ? 0 : 1;
                            playerCount += senderFaction.getOfficers().isEmpty() ? 0 : senderFaction.getOfficers().size();
                            playerCount += senderFaction.getMembers().isEmpty() ? 0 : senderFaction.getMembers().size();
                            playerCount += senderFaction.getRecruits().isEmpty() ? 0 : senderFaction.getRecruits().size();

                            if(playerCount >= getPlugin().getConfiguration().getConfigFields().getPlayerLimit())
                            {
                                senderPlayer.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_INVITE_MORE_PLAYERS_TO_YOUR_FACTION + " " + PluginMessages.FACTIONS_PLAYER_LIMIT_HAS_BEEN_REACHED));
                                return CommandResult.success();
                            }
                        }

                        if(!getPlugin().getFactionLogic().getFactionByPlayerUUID(invitedPlayer.getUniqueId()).isPresent())
                        {
                            try
                            {
                                Invite invite = new Invite(senderFaction.getName(), invitedPlayer.getUniqueId());
                                EagleFactions.INVITE_LIST.add(invite);

                                invitedPlayer.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, senderFaction.getName(), TextColors.GREEN, " " + PluginMessages.HAS_SENT_YOU_AN_INVITE + " " + PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT +
                                        " " + PluginMessages.TYPE + " ", TextColors.GOLD, "/f join " + senderFaction.getName(), TextColors.WHITE, " " + PluginMessages.TO_JOIN));

                                senderPlayer.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, PluginMessages.YOU_INVITED + " ", TextColors.GOLD, invitedPlayer.getName(), TextColors.GREEN, " " + PluginMessages.TO_YOUR_FACTION));

                                //TODO: Create a separate listener for removing invitations.

                                Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

                                taskBuilder.execute(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if(EagleFactions.INVITE_LIST.contains(invite) && EagleFactions.INVITE_LIST != null)
                                        {
                                            EagleFactions.INVITE_LIST.remove(invite);
                                        }
                                    }
                                }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(EagleFactions.getPlugin());

                                return CommandResult.success();
                            }
                            catch (Exception exception)
                            {
                                exception.printStackTrace();
                            }

                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PLAYER_IS_ALREADY_IN_A_FACTION));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_INVITE_PLAYERS_TO_FACTION));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                }
            }
            else
            {
                source.sendMessage (Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f invite <player>"));
        }

        return CommandResult.success();


    }
}
