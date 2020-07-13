/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 27/10/2019 02:17.
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

package br.net.fabiozumbi12.RedProtect.Bukkit.helpers;


public enum Particle {
    EXPLOSION_NORMAL("explode", 0),
    EXPLOSION_LARGE("largeexplode", 1),
    EXPLOSION_HUGE("hugeexplosion", 2),
    FIREWORKS_SPARK("fireworksSpark", 3),
    WATER_BUBBLE("bubble", 4),
    WATER_SPLASH("splash", 5),
    WATER_WAKE("wake", 6),
    SUSPENDED("suspended", 7),
    SUSPENDED_DEPTH("depthsuspend", 8),
    CRIT("crit", 9),
    CRIT_MAGIC("magicCrit", 10),
    SMOKE_NORMAL("smoke", 11),
    SMOKE_LARGE("largesmoke", 12),
    SPELL("spell", 13),
    SPELL_INSTANT("instantSpell", 14),
    SPELL_MOB("mobSpell", 15),
    SPELL_MOB_AMBIENT("mobSpellAmbient", 16),
    SPELL_WITCH("witchMagic", 17),
    DRIP_WATER("dripWater", 18),
    DRIP_LAVA("dripLava", 19),
    VILLAGER_ANGRY("angryVillager", 20),
    VILLAGER_HAPPY("happyVillager", 21),
    TOWN_AURA("townaura", 22),
    NOTE("note", 23),
    PORTAL("portal", 24),
    ENCHANTMENT_TABLE("enchantmenttable", 25),
    FLAME("flame", 26),
    LAVA("lava", 27),
    FOOTSTEP("footstep", 28),
    CLOUD("cloud", 29),
    REDSTONE("reddust", 30),
    SNOWBALL("snowballpoof", 31),
    SNOW_SHOVEL("snowshovel", 32),
    SLIME("slime", 33),
    HEART("heart", 34),
    BARRIER("barrier", 35),
    ITEM_CRACK("iconcrack_", 36),
    BLOCK_CRACK("blockcrack_", 37),
    BLOCK_DUST("blockdust_", 38),
    WATER_DROP("droplet", 39),
    ITEM_TAKE("take", 40),
    MOB_APPEARANCE("mobappearance", 41);

    private final String clientName;
    private final int id;

    Particle(String clientName, int id) {
        this.clientName = clientName;
        this.id = id;
    }

    /**
     * @return the particle client name
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @return the particle id
     */
    public int getId() {
        return id;
    }
}
