package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.TargetEntityEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class BlockBreakListener extends AbstractListener
{
    private final ConfigFields configFields;

    public BlockBreakListener(EagleFactions plugin)
    {
        super(plugin);
        this.configFields = plugin.getConfiguration().getConfigFields();
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPre(ChangeBlockEvent.Pre event)
    {
        User user = null;
        if(event.getCause().containsType(Player.class))
        {
            user = event.getCause().first(Player.class).get();
        }
        else if(event.getCause().containsType(User.class))
        {
            user = event.getCause().first(User.class).get();
        }

        final LocatableBlock locatableBlock = event.getCause().first(LocatableBlock.class).orElse(null);
        final TileEntity tileEntity = event.getCause().first(TileEntity.class).orElse(null);
        final boolean pistonExtend = event.getContext().containsKey(EventContextKeys.PISTON_EXTEND);
        final boolean isLiquidSource = event.getContext().containsKey(EventContextKeys.LIQUID_FLOW);
        final boolean isFireSource = !isLiquidSource && event.getContext().containsKey(EventContextKeys.FIRE_SPREAD);
        final boolean isLeafDecay = event.getContext().containsKey(EventContextKeys.LEAVES_DECAY);
        final boolean isForgePlayerBreak = event.getContext().containsKey(EventContextKeys.PLAYER_BREAK);
        final Location<World> sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;

        if(user instanceof Player)
        {
            if(EagleFactions.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
            {
                Player player = (Player)user;
                if(locatableBlock != null)
                {
                    player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "LocatableBlock: ", TextColors.GOLD, locatableBlock.getBlockState().getType().getName())));
                }
                if(sourceLocation != null)
                {
                    player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "SourceBlock: ", TextColors.GOLD, sourceLocation.getBlockType().getName())));
                }
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "isForgePlayerBreak: ", TextColors.GOLD, isForgePlayerBreak)));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "EventContext: ", TextColors.GOLD, event.getContext())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "Cause: ", TextColors.GOLD, event.getCause())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "Event: ", TextColors.GOLD, event)));
            }
        }

        if(isForgePlayerBreak)
        {
            //Helps blocking mining laser from IC2
            if(user == null)
            {
                user = event.getContext().get(EventContextKeys.OWNER)
                        .orElse(event.getContext().get(EventContextKeys.NOTIFIER).orElse(null));

                if(user != null)
                {
                    if(EagleFactions.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
                    {
                        Player player = (Player)user;
                        if(locatableBlock != null)
                        {
                            player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "LocatableBlock: ", TextColors.GOLD, locatableBlock.getBlockState().getType().getName())));
                        }
                        if(sourceLocation != null)
                        {
                            player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "SourceBlock: ", TextColors.GOLD, sourceLocation.getBlockType().getName())));
                        }
                        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "isForgePlayerBreak: ", TextColors.GOLD, isForgePlayerBreak)));
                        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "EventContext: ", TextColors.GOLD, event.getContext())));
                        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "Cause: ", TextColors.GOLD, event.getCause())));
                        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.BLUE, "Event: ", TextColors.GOLD, event)));
                    }
                }
            }

            if(user instanceof Player)
            {
                for(Location<World> location : event.getLocations())
                {
                    if(location.getBlockType() == BlockTypes.AIR)
                        continue;

                    if(!super.getPlugin().getProtectionManager().canBreak(location, user))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if(sourceLocation != null)
        {
            List<Location<World>> sourceLocations = event.getLocations();
            if(pistonExtend)
            {
                sourceLocations = new ArrayList<>(event.getLocations());
                final Location<World> location = sourceLocations.get(sourceLocations.size() - 1);
                final Direction direction = locatableBlock.getLocation().getBlock().get(Keys.DIRECTION).get();
                final Location<World> directionLocation = location.getBlockRelative(direction);
                sourceLocations.add(directionLocation);
            }
            for(Location<World> location : sourceLocations)
            {
                if(user != null && pistonExtend)
                {
                    if(!super.getPlugin().getProtectionManager().canInteractWithBlock(location, user))
                    {
                        event.setCancelled(true);
                        return;
                    }
//                    Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
//                    Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
//                    if(optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent())
//                    {
//                        if(super.getPlugin().getFlagManager().canInteract(user.getUniqueId(), optionalPlayerFaction.get(), optionalChunkFaction.get()))
//                        {
//                            event.setCancelled(true);
//                        }
//                    }
                }

                if(isFireSource)
                {
                    final Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
                    if(optionalChunkFaction.isPresent() && optionalChunkFaction.get().getName().equalsIgnoreCase("SafeZone"))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }

                if(isLiquidSource)
                    continue;

                if(isLeafDecay)
                    continue;

                if(location.getBlock().getType() == BlockTypes.AIR)
                    continue;

                if(user != null && !super.getPlugin().getProtectionManager().canBreak(location, user))
                {
                    event.setCancelled(true);
                    return;
                }
                else if(user == null && !super.getPlugin().getProtectionManager().canBreak(location))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        else if(user != null)
        {
            for(Location<World> location : event.getLocations())
            {
                if(pistonExtend)
                {
                    if(!super.getPlugin().getProtectionManager().canInteractWithBlock(location, user))
                    {
                        event.setCancelled(true);
                    }
                }

                if(isFireSource)
                {
                    Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
                    if(configFields.getSafeZoneWorldNames().contains(location.getExtent().getName()) || (optionalChunkFaction.isPresent() && optionalChunkFaction.get().getName().equalsIgnoreCase("SafeZone")))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }

                if(isLiquidSource)
                    continue;

                if(isLeafDecay)
                    continue;

                //TODO: This is ran even when player right clicks the block.
                if(!super.getPlugin().getProtectionManager().canBreak(location, user))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreak(ChangeBlockEvent.Break event)
    {
        if(event instanceof ExplosionEvent || event.getCause().containsType(Explosion.class))
            return;

        User user = null;
        if(event.getCause().containsType(Player.class))
        {
            user = event.getCause().first(Player.class).get();
        }
        else if(event.getCause().containsType(User.class))
        {
            user = event.getCause().first(User.class).get();
        }

        //Helps blocking dynamite from IC2
        if(user == null)
        {
            user = event.getContext().get(EventContextKeys.OWNER).orElse(null);
        }

        LocatableBlock locatableBlock = null;
        if(event.getSource() instanceof LocatableBlock)
        {
            locatableBlock = (LocatableBlock) event.getSource();
        }
        if(locatableBlock != null)
        {
            if(locatableBlock.getBlockState().getType() == BlockTypes.FLOWING_WATER || locatableBlock.getBlockState().getType() == BlockTypes.FLOWING_LAVA)
                return;

            Optional<Faction> optionalSourceChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(locatableBlock.getLocation().getExtent().getUniqueId(), locatableBlock.getLocation().getChunkPosition());
            if(!optionalSourceChunkFaction.isPresent())
                return;
        }

        for(Transaction<BlockSnapshot> transaction : event.getTransactions())
        {
            final Location<World> location = transaction.getOriginal().getLocation().orElse(null);

            if(location == null || transaction.getOriginal().getState().getType() == BlockTypes.AIR)
            {
                continue;
            }

            if(user != null && !super.getPlugin().getProtectionManager().canBreak(location, user))
            {
                event.setCancelled(true);
                return;
            }
            else if(user == null && !super.getPlugin().getProtectionManager().canBreak(location))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockCollide(CollideBlockEvent event)
    {
        if(event instanceof CollideBlockEvent.Impact)
            return;

        if(event.getSource() instanceof FallingBlock)
            return;

        User user = null;
        final Cause cause = event.getCause();
        final EventContext context = event.getContext();
        if (cause.root() instanceof TileEntity) {
            user = context.get(EventContextKeys.OWNER)
                    .orElse(context.get(EventContextKeys.NOTIFIER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        } else {
            user = context.get(EventContextKeys.NOTIFIER)
                    .orElse(context.get(EventContextKeys.OWNER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        }

        if (user == null) {
            if (event instanceof ExplosionEvent) {
                // Check igniter
                final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
                if (living instanceof User) {
                    user = (User) living;
                }
            }
        }

        if(user == null)
            return;

        final BlockType blockType = event.getTargetBlock().getType();
        if(blockType.equals(BlockTypes.AIR))
            return;

        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(event.getTargetLocation().getExtent().getUniqueId(), event.getTargetLocation().getChunkPosition());

        if(optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent())
        {
            if(!optionalChunkFaction.get().getName().equalsIgnoreCase(optionalPlayerFaction.get().getName()))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onProjectileImpactBlock(CollideBlockEvent.Impact event)
    {
        if(!(event.getSource() instanceof Entity))
            return;

        User user = null;
        final Cause cause = event.getCause();
        final EventContext context = event.getContext();
        if (cause.root() instanceof TileEntity) {
            user = context.get(EventContextKeys.OWNER)
                    .orElse(context.get(EventContextKeys.NOTIFIER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        } else {
            user = context.get(EventContextKeys.NOTIFIER)
                    .orElse(context.get(EventContextKeys.OWNER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        }

        if (user == null) {
            if (event instanceof ExplosionEvent) {
                // Check igniter
                final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
                if (living instanceof User) {
                    user = (User) living;
                }
            }
        }

        if(user == null)
            return;

        Location<World> impactPoint = event.getImpactPoint();
        Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(impactPoint.getExtent().getUniqueId(), impactPoint.getChunkPosition());

        if(!optionalChunkFaction.isPresent())
            return;

        Faction chunkFaction = optionalChunkFaction.get();
        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if(!optionalPlayerFaction.isPresent())
        {
            event.setCancelled(true);
            return;
        }

        Faction playerFaction = optionalPlayerFaction.get();
        if(playerFaction.getName().equalsIgnoreCase(chunkFaction.getName()))
            return;
        else
        {
            event.setCancelled(true);
            return;
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityCollideEntity(CollideEntityEvent event)
    {
        final List<Entity> entityList = event.getEntities();
        for(final Entity entity : entityList)
        {
            if(entity instanceof Player && event.getSource() instanceof Entity)
            {
                final Entity sourceEntity = (Entity) event.getSource();
                if(sourceEntity.getType().getName().contains("projectile"))
                {
                    final Player player = (Player) entity;
                    if(configFields.getSafeZoneWorldNames().contains(player.getWorld().getName()))
                    {
                        sourceEntity.remove();
                        event.setCancelled(true);
                        return;
                    }

                    final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
                    if(optionalChunkFaction.isPresent() && optionalChunkFaction.get().getName().equals("SafeZone"))
                    {
                        sourceEntity.remove();
                        event.setCancelled(true);
                        return;
                    }

                    //TechGuns - Should be better to find more generic way of doing this...
                    if(sourceEntity.getType().getId().contains("techguns"))
                    {
                        //If sourceEntity = projectile that comes from techguns
                        final Class sourceEntityClass = sourceEntity.getClass();
                        try
                        {
                            Player shooterPlayer = null;
                            final Field[] fields = sourceEntityClass.getDeclaredFields();
                            for(Field field : fields)
                            {
                                if(field.getName().equals("shooter"))
                                {
                                    field.setAccessible(true);
                                    final Object playerObject = field.get(sourceEntity);
                                    if(playerObject instanceof Player)
                                    {
                                        shooterPlayer = (Player) playerObject;
                                    }
                                    field.setAccessible(false);
                                }
                            }

                            if(shooterPlayer != null)
                            {
                                //Crazy situation...
                                if(shooterPlayer == player)
                                    continue;

                                //We got shooter player
                                //Check friendly fire
                                final boolean isFactionFriendlyFireOn = configFields.isFactionFriendlyFire();
                                final boolean isAllianceFriendlyFireOn = configFields.isAllianceFriendlyFire();
                                if(isFactionFriendlyFireOn && isAllianceFriendlyFireOn)
                                    continue;

                                final Optional<Faction> optionalAffectedPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                                final Optional<Faction> optionalShooterPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(shooterPlayer.getUniqueId());

                                if(optionalAffectedPlayerFaction.isPresent() && optionalShooterPlayerFaction.isPresent())
                                {
                                    final Faction affectedPlayerFaction = optionalAffectedPlayerFaction.get();
                                    final Faction shooterPlayerFaction = optionalShooterPlayerFaction.get();

                                    if(!isFactionFriendlyFireOn)
                                    {
                                        if(affectedPlayerFaction.getName().equals(shooterPlayerFaction.getName()))
                                        {
                                            sourceEntity.remove();
                                            event.setCancelled(true);
                                            return;
                                        }
                                    }

                                    if(!isAllianceFriendlyFireOn)
                                    {
                                        if(affectedPlayerFaction.getAlliances().contains(shooterPlayerFaction.getName()))
                                        {
                                            sourceEntity.remove();
                                            event.setCancelled(true);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        catch(IllegalAccessException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if(event instanceof CollideEntityEvent.Impact)
            return;

        //Handle Item Frames
        Object rootCause = event.getCause().root();
        if(!(rootCause instanceof ItemFrame))
            return;

        event.filterEntities(new Predicate<Entity>()
        {
            @Override
            public boolean test(Entity entity)
            {
                if(entity instanceof Living)
                {
                    if(entity instanceof User && !getPlugin().getProtectionManager().canInteractWithBlock(entity.getLocation(), (User)entity))
                    {
                        return false;
                    }
                }
                return true;
            }
        });
    }
}
