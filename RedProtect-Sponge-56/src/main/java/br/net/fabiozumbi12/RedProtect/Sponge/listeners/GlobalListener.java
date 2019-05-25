/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RedProtectUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.*;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.golem.Golem;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.entity.vehicle.minecart.TNTMinecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.*;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.world.ChangeWorldWeatherEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.weather.Weathers;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GlobalListener {

    public GlobalListener() {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Loaded GlobalListener...");
    }

    /**
     * @param p   - Player
     * @param b   - Block
     * @param fat - 1 = Place Block | 2 = Break Block
     * @return Boolean - Can build or not.
     */
    private boolean bypassBuild(Player p, BlockSnapshot b, int fat) {
        if (p.hasPermission("redprotect.bypass.world"))
            return true;

        if (RedProtect.get().config.needClaimToBuild(p, b))
            return false;

        return (fat == 1 && canPlaceList(p.getWorld(), b.getState().getType().getName())) ||
                (fat == 2 && canBreakList(p.getWorld(), b.getState().getType().getName())) ||
                RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).build;
    }

    private boolean canPlaceList(World w, String type) {
        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).build) {
            //blacklist
            List<String> blt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.place_blocks.blacklist;
            if (!blt.isEmpty()) return blt.stream().noneMatch(type::matches);

            //whitelist
            List<String> wlt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.place_blocks.whitelist;
            if (!wlt.isEmpty()) return wlt.stream().anyMatch(type::matches);

            return false;
        }
        return true;
    }

    private boolean canBreakList(World w, String type) {
        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).build) {
            //blacklist
            List<String> blt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.break_blocks.blacklist;
            if (!blt.isEmpty()) return blt.stream().noneMatch(type::matches);

            //whitelist
            List<String> wlt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.break_blocks.whitelist;
            if (!wlt.isEmpty()) return wlt.stream().anyMatch(type::matches);

            return false;
        }
        return true;
    }

    private boolean canInteractBlocksList(World w, String type) {
        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).interact) {
            //blacklist
            List<String> blt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_interact_false.interact_blocks.blacklist;
            if (!blt.isEmpty()) return blt.stream().noneMatch(type::matches);

            //whitelist
            List<String> wlt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_interact_false.interact_blocks.whitelist;
            if (!wlt.isEmpty()) return wlt.stream().anyMatch(type::matches);

            return false;
        }
        return true;
    }

    private boolean canInteractEntitiesList(World w, String type) {
        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).interact) {
            //blacklist
            List<String> blt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_interact_false.interact_entities.blacklist;
            if (!blt.isEmpty()) return blt.stream().noneMatch(type::matches);

            //whitelist
            List<String> wlt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_interact_false.interact_entities.whitelist;
            if (!wlt.isEmpty()) return wlt.stream().anyMatch(type::matches);

            return false;
        }
        return true;
    }

    @Listener
    public void onPlayerMove(MoveEntityEvent e){
        if (e.getTargetEntity() instanceof Player){
            Player p = (Player) e.getTargetEntity();

            //set velocities
            if (!p.hasPermission("redprotect.bypass.velocity")) {
                if (RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_velocity.walk_speed >= 0) {
                    double vel = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_velocity.walk_speed;
                    p.offer(Keys.WALKING_SPEED, vel);
                }
                if (RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_velocity.fly_speed >= 0) {
                    double vel = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_velocity.fly_speed;
                    p.offer(Keys.FLYING_SPEED, vel);
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void PlayerDropItem(DropItemEvent.Dispense e, @Root Player p) {
        for (Entity ent : e.getEntities()) {
            Location<World> l = ent.getLocation();
            Region r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());

            if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_candrop && !p.hasPermission("redprotect.world.bypass")) {
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onIceForm(ChangeBlockEvent e){
        Region r = RedProtect.get().rm.getTopRegion(e.getTransactions().get(0).getFinal().getLocation().get(), this.getClass().getName());

        if (r == null && (e.getTransactions().get(0).getOriginal().getState().getType().equals(BlockTypes.ICE) ||
                e.getTransactions().get(0).getOriginal().getState().getType().equals(BlockTypes.FROSTED_ICE))){
            if (e.getCause().containsType(Player.class)){
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e.getTransactions().get(0).getFinal().getLocation().get().getExtent().getName()).iceform_by.player) {
                    e.setCancelled(true);
                }
            } else if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e.getTransactions().get(0).getFinal().getLocation().get().getExtent().getName()).iceform_by.entity){
                e.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemPickup(CollideEntityEvent event, @Root Player p) {
        for (Entity ent : event.getEntities()) {
            if (!(ent instanceof Item)) {
                continue;
            }
            Region r = RedProtect.get().rm.getTopRegion(ent.getLocation(), this.getClass().getName());
            if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_canpickup && !p.hasPermission("redprotect.world.bypass")) {
                event.setCancelled(true);
            }
        }
    }


    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onFlow(ChangeBlockEvent.Pre e, @First LocatableBlock locatable) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "Is BlockListener - onFlow event");

        BlockState sourceState = locatable.getBlockState();

        //liquid check
        MatterProperty mat = sourceState.getProperty(MatterProperty.class).orElse(null);
        if (mat != null && mat.getValue() == MatterProperty.Matter.LIQUID) {
            e.getLocations().forEach(loc -> {
                Region r = RedProtect.get().rm.getTopRegion(loc, this.getClass().getName());
                if (r == null) {
                    boolean flow = RedProtect.get().config.globalFlagsRoot().worlds.get(locatable.getLocation().getExtent().getName()).allow_changes_of.liquid_flow;
                    boolean allowWater = RedProtect.get().config.globalFlagsRoot().worlds.get(locatable.getLocation().getExtent().getName()).allow_changes_of.water_flow;
                    boolean allowLava = RedProtect.get().config.globalFlagsRoot().worlds.get(locatable.getLocation().getExtent().getName()).allow_changes_of.lava_flow;

                    if (!flow)
                        e.setCancelled(true);
                    if (!allowWater && (loc.getBlockType() == BlockTypes.WATER || loc.getBlockType() == BlockTypes.FLOWING_WATER))
                        e.setCancelled(true);
                    if (!allowLava && (loc.getBlockType() == BlockTypes.LAVA || loc.getBlockType() == BlockTypes.FLOWING_LAVA))
                        e.setCancelled(true);
                }
            });
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreakGeneric(ChangeBlockEvent.Break e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "GlobalListener - Is onBlockBreakGeneric event");

        LocatableBlock locatable = e.getCause().first(LocatableBlock.class).orElse(null);
        if (locatable != null) {
            BlockState sourceState = locatable.getBlockState();

            //liquid check
            MatterProperty mat = sourceState.getProperty(MatterProperty.class).orElse(null);
            if (mat != null && mat.getValue() == MatterProperty.Matter.LIQUID) {
                boolean allowdamage = RedProtect.get().config.globalFlagsRoot().worlds.get(locatable.getLocation().getExtent().getName()).allow_changes_of.flow_damage;

                Region r = RedProtect.get().rm.getTopRegion(locatable.getLocation(), this.getClass().getName());
                if (r == null && !allowdamage && locatable.getLocation().getBlockType() != BlockTypes.AIR) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onDecay(ChangeBlockEvent.Decay e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "GlobalListener - Is onDecay event");

        BlockSnapshot bfrom = e.getTransactions().get(0).getOriginal();
        boolean allowDecay = RedProtect.get().config.globalFlagsRoot().worlds.get(bfrom.getLocation().get().getExtent().getName()).allow_changes_of.leaves_decay;
        Region r = RedProtect.get().rm.getTopRegion(bfrom.getLocation().get(), this.getClass().getName());
        if (r == null && !allowDecay) {
            e.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPlace(ChangeBlockEvent.Place e, @Root Player p) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is ChangeBlockEvent event! Cancelled? " + false);

        BlockSnapshot b = e.getTransactions().get(0).getFinal();
        ItemType item = RedProtect.get().getVersionHelper().getItemInHand(p);
        Region r = RedProtect.get().rm.getTopRegion(e.getTransactions().get(0).getOriginal().getLocation().get(), this.getClass().getName());

        if (r != null) {
            return;
        }

        if (!RedProtectUtil.canBuildNear(p, b.getLocation().get())) {
            e.setCancelled(true);
            return;
        }

        if (item.getName().contains("minecart") || item.getName().contains("boat")) {
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).use_minecart && !p.hasPermission("redprotect.world.bypass")) {
                e.setCancelled(true);
                RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Can't place minecart/boat!");
            }
        } else {
            if (!bypassBuild(p, b, 1)) {
                e.setCancelled(true);
                RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Can't Build!");
            }
        }
    }

    private final HashMap<World, Integer> rainCounter = new HashMap<>();
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onChangeWeather(ChangeWorldWeatherEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "GlobalListener - Is onChangeWeather event");

        World w = e.getTargetWorld();
        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e.getTargetWorld().getName()).weather.allow_weather && !e.getWeather().equals(Weathers.CLEAR)) {
            e.setCancelled(true);
        }

        int attempts = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).weather.attempts_before_rain;
        if (!e.getWeather().equals(Weathers.CLEAR)) {
            if (!rainCounter.containsKey(w)) {
                rainCounter.put(w, attempts);
                e.setCancelled(true);
            } else {
                int acTry = rainCounter.get(w);
                if (acTry - 1 <= 0) {
                    Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> w.setWeather(e.getWeather(), RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).weather.rain_time), RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).weather.rain_time, TimeUnit.SECONDS);
                    rainCounter.put(w, attempts);
                } else {
                    rainCounter.put(w, acTry - 1);
                    e.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteract(InteractEvent e, @Root Player p) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is InteractEvent event! Cancelled? " + false);
        if (!e.getInteractionPoint().isPresent()) {
            return;
        }
        Location<World> loc = new Location<>(p.getWorld(), e.getInteractionPoint().get());
        Region r = RedProtect.get().rm.getTopRegion(loc, this.getClass().getName());

        if (!canUse(p, r)) {
            e.setCancelled(true);
            return;
        }

        if (r != null) {
            return;
        }

        if (e instanceof InteractEntityEvent) {
            Entity ent = ((InteractEntityEvent) e).getTargetEntity();
            RedProtect.get().logger.debug(LogLevel.ENTITY, "GlobalListener - Entity: " + ent.getType().getName());
            if (ent instanceof Minecart || ent instanceof Boat) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).use_minecart && !p.hasPermission("redprotect.world.bypass")) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (ent instanceof Hanging || ent instanceof ArmorStand) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).build && !p.hasPermission("redprotect.world.bypass")) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (ent instanceof Monster && !RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).if_interact_false.entity_monsters && !p.hasPermission("redprotect.world.bypass")) {
                e.setCancelled(true);
                return;
            }
            if (ent instanceof Animal || ent instanceof Golem || ent instanceof Ambient || ent instanceof Aquatic) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).if_interact_false.entity_passives && !p.hasPermission("redprotect.world.bypass")) {
                    e.setCancelled(true);
                    return;
                }
            }
            if ((loc.getBlock().getType().equals(BlockTypes.PUMPKIN_STEM) ||
                    loc.getBlock().getType().equals(BlockTypes.MELON_STEM) ||
                    loc.getBlock().getType().getName().contains("chorus_") ||
                    loc.getBlock().getType().getName().contains("beetroot_") ||
                    loc.getBlock().getType().getName().contains("sugar_cane"))
                    && !RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).allow_crop_trample && !p.hasPermission("redprotect.bypass.world")){
                e.setCancelled(true);
                return;
            }
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).interact) {
                if (!p.hasPermission("redprotect.world.bypass") && !(ent instanceof Player)) {
                    if (!canInteractEntitiesList(p.getWorld(), ent.getType().getName())) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (e instanceof InteractBlockEvent) {
            InteractBlockEvent eb = (InteractBlockEvent) e;
            String bname = eb.getTargetBlock().getState().getType().getName();
            RedProtect.get().logger.debug(LogLevel.BLOCKS, "GlobalListener - Block: " + bname);
            if (bname.contains("rail") || bname.contains("water")) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).use_minecart && p.hasPermission("redprotect.world.bypass")) {
                    e.setCancelled(true);
                }
            } else {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).interact) {
                    if (!p.hasPermission("redprotect.world.bypass")) {
                        if (!canInteractBlocksList(p.getWorld(), bname)) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
                if ((!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).build && !p.hasPermission("redprotect.world.bypass"))
                        && bname.contains("leaves")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreakGlobal(ChangeBlockEvent.Break e, @Root Player p) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is BlockBreakEvent event! Cancelled? " + false);

        BlockSnapshot bt = e.getTransactions().get(0).getOriginal();
        Region r = RedProtect.get().rm.getTopRegion(bt.getLocation().get(), this.getClass().getName());
        if (r != null) {
            return;
        }

        if (!RedProtectUtil.canBuildNear(p, bt.getLocation().get())) {
            e.setCancelled(true);
            return;
        }

        if (!bypassBuild(p, bt, 2)) {
            e.setCancelled(true);
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Can't Break!");
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBurnGlobal(ChangeBlockEvent.Modify e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "GlobalListener - Is onBlockBurnGlobal event");

        Transaction<BlockSnapshot> b = e.getTransactions().get(0);

        if (e.getCause().first(Monster.class).isPresent()) {
            Region r = RedProtect.get().rm.getTopRegion(b.getOriginal().getLocation().get(), this.getClass().getName());
            if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(b.getOriginal().getLocation().get().getExtent().getName()).use_minecart) {
                e.setCancelled(true);
            }
        }
    }


    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onFireSpread(ChangeBlockEvent.Place e, @First LocatableBlock locatable) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "GlobalListener - Is onFireSpread event!");

        BlockState sourceState = locatable.getBlockState();

        if (e.getTransactions().get(0).getFinal().getState().getType().equals(BlockTypes.FIRE) &&
                (sourceState.getType() == BlockTypes.FIRE || sourceState.getType() == BlockTypes.LAVA || sourceState.getType() == BlockTypes.FLOWING_LAVA)) {
            boolean fireDamage = RedProtect.get().config.globalFlagsRoot().worlds.get(locatable.getLocation().getExtent().getName()).fire_spread;
            if (!fireDamage) {
                Region r = RedProtect.get().rm.getTopRegion(e.getTransactions().get(0).getOriginal().getLocation().get(), this.getClass().getName());
                if (r == null) {
                    RedProtect.get().logger.debug(LogLevel.BLOCKS, "Tryed to PLACE FIRE!");
                    e.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onFireSpread(ChangeBlockEvent.Break e, @First LocatableBlock locatable) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "GlobalListener - Is onBlockBreakGeneric event");

        BlockState sourceState = locatable.getBlockState();

        if (sourceState.getType() == BlockTypes.FIRE) {
            BlockSnapshot b = e.getTransactions().get(0).getOriginal();
            boolean fireDamage = RedProtect.get().config.globalFlagsRoot().worlds.get(locatable.getLocation().getExtent().getName()).fire_block_damage;
            if (!fireDamage && b.getState().getType() != BlockTypes.FIRE) {
                Region r = RedProtect.get().rm.getTopRegion(b.getLocation().get(), this.getClass().getName());
                if (r == null) {
                    RedProtect.get().logger.debug(LogLevel.BLOCKS, "Tryed to break from FIRE!");
                    e.setCancelled(true);
                }
            }
        }
    }
/*
	@Listener(order = Order.FIRST, beforeModifications = true)	
    public void onPlayerInteract(InteractEntityEvent e, @Root Player p) {
		RedProtect.get().logger.debug(LogLevel.BLOCKS,"GlobalListener - Is onPlayerInteract event");

        Entity ent = e.getTargetEntity();
        Location<World> l = ent.getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());
                
        if (!canUse(p, r)){
        	e.setCancelled(true);
        }
        
        if (r != null){
			return;
		}

	}*/

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBucketUse(UseItemStackEvent.Start e, @Root Player p) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "GlobalListener - Is onBucketUse event");

        Location<World> l = p.getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());

        if (!canUse(p, r)) {
            e.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onProjectileHit(CollideEntityEvent event, @Root Projectile proj) {
        RedProtect.get().logger.debug(LogLevel.ENTITY, "Is CollideEntityEvent(onProjectileHit) event.");
        RedProtect.get().logger.debug(LogLevel.ENTITY, "Projectile: " + proj.getType().getName());
        if (RedProtect.get().rm.getTopRegion(proj.getLocation(), this.getClass().getName()) != null) return;

        for (Entity ent : event.getEntities()) {
            RedProtect.get().logger.debug(LogLevel.ENTITY, "Entity: " + ent.getType().getName());

            if (proj.getShooter() instanceof Player) {
                Player p = (Player) proj.getShooter();

                if (ent instanceof Player) {
                    if (!p.equals(ent) && !RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).pvp && !p.hasPermission("redprotect.world.bypass")) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (ent instanceof Animal || ent instanceof Villager || ent instanceof Golem || ent instanceof Ambient) {
                    if (!RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).player_hurt_passives && !p.hasPermission("redprotect.world.bypass")) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (ent instanceof Monster) {
                    if (!RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).player_hurt_monsters && !p.hasPermission("redprotect.world.bypass")) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (ent instanceof Hanging || ent instanceof ArmorStand) {
                    if (!RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).build && !p.hasPermission("redprotect.world.bypass")) {
                        event.setCancelled(true);
                    }
                }
            } else {
                if (ent instanceof Hanging || ent instanceof ArmorStand) {
                    if (!RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).entity_block_damage) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityDamageEntity(DamageEntityEvent e) {
        Entity e1 = e.getTargetEntity();
        Entity e2;

        RedProtect.get().logger.debug(LogLevel.ENTITY, "GlobalListener: DamageEntityEvent - e1: " + e1.getType().getName());


        if (e1 instanceof Living && !(e1 instanceof Monster)) {
            Region r = RedProtect.get().rm.getTopRegion(e1.getLocation(), this.getClass().getName());
            if (r == null && RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).invincible) {
                e.setCancelled(true);
            }
        }

        if (e.getCause().first(Living.class).isPresent()) {
            e2 = e.getCause().first(Living.class).get();
            RedProtect.get().logger.debug(LogLevel.ENTITY, "GlobalListener: DamageEntityEvent - Is DamageEntityEvent event. Damager " + e2.getType().getName());
        } else {
            return;
        }

        RedProtect.get().logger.debug(LogLevel.ENTITY, "GlobalListener: DamageEntityEvent - e1: " + e1.getType().getName() + " - e2: " + e2.getType().getName());

        Location<World> loc = e1.getLocation();

        if (e2 instanceof Projectile) {
            Region r1 = RedProtect.get().rm.getTopRegion(loc, this.getClass().getName());
            if (r1 != null) {
                return;
            }

            Projectile proj = (Projectile) e2;
            if (proj.getShooter() instanceof Entity) {
                e2 = (Entity) proj.getShooter();
            }

            if (!(e2 instanceof Player)) {
                if (e1 instanceof Hanging || e1 instanceof ArmorStand) {
                    if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).entity_block_damage) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (e2 instanceof Creeper || e2 instanceof PrimedTNT || e2 instanceof TNTMinecart) {
            Region r1 = RedProtect.get().rm.getTopRegion(loc, this.getClass().getName());
            if (r1 != null) {
                return;
            }

            if (e1 instanceof Player) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).explosion_entity_damage) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Animal || e1 instanceof Villager || e1 instanceof Golem || e1 instanceof Ambient) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).explosion_entity_damage) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Monster) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).explosion_entity_damage) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Hanging || e1 instanceof ArmorStand) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).explosion_entity_damage) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (e2 instanceof Player) {
            Region r1 = RedProtect.get().rm.getTopRegion(loc, this.getClass().getName());
            if (r1 != null) {
                return;
            }

            Player p = (Player) e2;

            if (e1 instanceof Player) {
                if (!e1.equals(e2) && !RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).pvp && !p.hasPermission("redprotect.world.bypass")) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Animal || e1 instanceof Villager || e1 instanceof Golem || e1 instanceof Ambient) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).player_hurt_passives && !p.hasPermission("redprotect.world.bypass")) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Monster) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).player_hurt_monsters && !p.hasPermission("redprotect.world.bypass")) {
                    e.setCancelled(true);
                    return;
                }
            }

            if (e1 instanceof Boat || e1 instanceof Minecart) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).use_minecart && !p.hasPermission("redprotect.world.bypass")) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Hanging || e1 instanceof ArmorStand) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e1.getWorld().getName()).entity_block_damage && !p.hasPermission("redprotect.world.bypass")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityExplode(ExplosionEvent.Detonate e) {

        World w = e.getTargetWorld();
        for (Location<World> b : e.getAffectedLocations()) {
            Region r = RedProtect.get().rm.getTopRegion(b, this.getClass().getName());
            if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).entity_block_damage) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onFramePlace(SpawnEntityEvent event, @Root Player p) {
        for (Entity e : event.getEntities()) {
            if (e == null || RedProtect.get().rm.getTopRegion(e.getLocation(), this.getClass().getName()) != null) {
                continue;
            }

            if (event instanceof DropItemEvent) {
                continue;
            }

            if (e instanceof Hanging) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e.getWorld().getName()).build && !p.hasPermission("redprotect.world.bypass")) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @IsCancelled(Tristate.FALSE)
    public void onCreatureSpawn(SpawnEntityEvent event) {

        for (Entity e : event.getEntities()) {
            if (e == null || (RedProtect.get().rm.getTopRegion(e.getLocation(), this.getClass().getName()) != null &&
                    RedProtect.get().config.globalFlagsRoot().worlds.get(e.getWorld().getName()).spawn_allow_on_regions)) {
                continue;
            }

            //blacklist
            List<String> blacklist = RedProtect.get().config.globalFlagsRoot().worlds.get(e.getWorld().getName()).spawn_blacklist;
            if (e instanceof Monster && blacklist.contains("MONSTERS")) {
                RedProtect.get().logger.debug(LogLevel.SPAWN, "GlobalListener - Cancelled spawn of BLACKLISTED Monster " + e.getType().getName());
                event.setCancelled(true);
                return;
            }
            if ((e instanceof Animal || e instanceof Villager || e instanceof Ambient || e instanceof Golem) && blacklist.contains("PASSIVES")) {
                RedProtect.get().logger.debug(LogLevel.SPAWN, "GlobalListener - Cancelled spawn of BLACKLISTED Animal " + e.getType().getName());
                event.setCancelled(true);
                return;
            }
            if (blacklist.stream().anyMatch(e.getType().getName()::matches)) {
                RedProtect.get().logger.debug(LogLevel.SPAWN, "GlobalListener - Cancelled spawn of BLACKLISTED " + e.getType().getName());
                event.setCancelled(true);
                return;
            }

            //whitelist
            List<String> wtl = RedProtect.get().config.globalFlagsRoot().worlds.get(e.getWorld().getName()).spawn_whitelist;
            if (!wtl.isEmpty()) {
                if (e instanceof Monster && !wtl.contains("MONSTERS")) {
                    RedProtect.get().logger.debug(LogLevel.SPAWN, "GlobalListener - Cancelled spawn of NON WHITELISTED Monster " + e.getType().getName());
                    event.setCancelled(true);
                    return;
                }
                if ((e instanceof Animal || e instanceof Villager || e instanceof Ambient || e instanceof Golem) && !wtl.contains("PASSIVES")) {
                    RedProtect.get().logger.debug(LogLevel.SPAWN, "GlobalListener - Cancelled spawn of NON WHITELISTED Animal " + e.getType().getName());
                    event.setCancelled(true);
                    return;
                }
                if (wtl.stream().noneMatch(e.getType().getName()::matches)) {
                    RedProtect.get().logger.debug(LogLevel.SPAWN, "GlobalListener - Cancelled spawn of NON WHITELISTED " + e.getType().getName());
                    event.setCancelled(true);
                    return;
                }
            }
            RedProtect.get().logger.debug(LogLevel.SPAWN, "GlobalListener - Spawn mob " + e.getType().getName());
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockGrow(ChangeBlockEvent.Grow e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "GlobalListener - Is ChangeBlockEvent.Grow event");

        BlockSnapshot b = e.getTransactions().get(0).getOriginal();
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation().get(), this.getClass().getName());
        if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(b.getLocation().get().getExtent().getName()).block_grow) {
            e.setCancelled(true);
            RedProtect.get().logger.debug(LogLevel.BLOCKS, "Cancel grow " + b.getState().getName());
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onChangeWorld(MoveEntityEvent.Teleport e, @Root Player player) {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "GlobalListener - Is MoveEntityEvent.Teleport event");
        if (e.getFromTransform().getExtent() != e.getToTransform().getExtent()) {
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e.getFromTransform().getExtent().getName()).on_exit_cmds.isEmpty()) {
                for (String cmd : RedProtect.get().config.globalFlagsRoot().worlds.get(e.getFromTransform().getExtent().getName()).on_exit_cmds) {
                    if (cmd == null || cmd.isEmpty()) continue;
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    Sponge.getGame().getCommandManager().process(RedProtect.get().getServer().getConsole(),
                            cmd.replace("{world-from}", e.getFromTransform().getExtent().getName())
                                    .replace("{world-to}", e.getToTransform().getExtent().getName())
                                    .replace("{player}", player.getName()));
                }
            }
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e.getFromTransform().getExtent().getName()).on_enter_cmds.isEmpty()) {
                for (String cmd : RedProtect.get().config.globalFlagsRoot().worlds.get(e.getFromTransform().getExtent().getName()).on_enter_cmds) {
                    if (cmd == null || cmd.isEmpty()) continue;
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    Sponge.getGame().getCommandManager().process(RedProtect.get().getServer().getConsole(),
                            cmd.replace("{world-from}", e.getFromTransform().getExtent().getName())
                                    .replace("{world-to}", e.getToTransform().getExtent().getName())
                                    .replace("{player}", player.getName()));
                }
            }
        }
    }

    private boolean canUse(Player p, Region r) {
        boolean claimRps = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.allow_on_claimed_rps;
        boolean wilderness = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.allow_on_wilderness;

        ItemType item = RedProtect.get().getVersionHelper().getItemInHand(p);

        //deny item usage
        if (!RedProtect.get().ph.hasPerm(p, "redprotect.world.bypass") && !item.equals(ItemTypes.NONE) && RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.items.stream().anyMatch(item.getType().getName()::matches)) {
            if (r != null && ((!claimRps && r.canBuild(p)) || (claimRps && !r.canBuild(p)))) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                return false;
            }
            if (r == null && !wilderness) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                return false;
            }
        }
        return true;
    }
}
