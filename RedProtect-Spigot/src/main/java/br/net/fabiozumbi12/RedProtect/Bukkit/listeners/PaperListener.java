package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityKnockback(EntityKnockbackEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(e.getEntity().getLocation());
            if (r != null) {
                Player attacker = null;
                if (e instanceof EntityPushedByEntityAttackEvent ep && ep.getPushedBy() instanceof Player p) {
                    attacker = p;
                }
                if (!r.canPushAndPull(attacker)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}

