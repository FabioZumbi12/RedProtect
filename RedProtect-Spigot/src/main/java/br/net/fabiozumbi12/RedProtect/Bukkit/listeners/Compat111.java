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

package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class Compat111 implements Listener {

    static void sendBarMsg(String msg, String color, Player p) {
        BossBar bar = Bukkit.createBossBar(msg, BarColor.valueOf(color), BarStyle.SEGMENTED_10);
        bar.addPlayer(p);
        removeBar(bar, p);
    }

    private static void removeBar(final BossBar bar, final Player p) {
        final int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(RedProtect.get(), () -> {
            double d = bar.getProgress();
            if (d >= 0.2) {
                bar.setProgress(d - 0.2);
            }
        }, 20, 20);
        Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> {
            bar.removePlayer(p);
            Bukkit.getScheduler().cancelTask(task);
        }, 120);
    }

    @EventHandler
    private void onChorusBreak(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;

        if (event.getHitBlock() != null && event.getHitBlock().getType() == Material.CHORUS_FLOWER) {
            Player p = (Player) event.getEntity().getShooter();
            Region r = RedProtect.get().getRegionManager().getTopRegion(event.getHitBlock().getLocation());
            if (r != null && !r.canBuild(p)) {
                event.getEntity().remove();
                event.getHitBlock().setType(Material.AIR);
                Bukkit.getScheduler().runTask(RedProtect.get(), () -> event.getHitBlock().setType(Material.CHORUS_FLOWER));
                RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantbreak");
            }
        }
    }
}
