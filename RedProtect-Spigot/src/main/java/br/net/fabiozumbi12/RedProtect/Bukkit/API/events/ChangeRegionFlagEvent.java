/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 10/05/2023 14:49
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

package br.net.fabiozumbi12.RedProtect.Bukkit.API.events;

import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChangeRegionFlagEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Region region;
    private final String flag;
    private final CommandSender cause;
    private Object value;
    private boolean isCancelled = false;

    public ChangeRegionFlagEvent(CommandSender cause, Region region, String flag, Object value) {
        this.region = region;
        this.flag = flag;
        this.value = value;
        this.cause = cause;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Region getRegion() {
        return this.region;
    }

    public String getFlag() {
        return this.flag;
    }

    public Object getFlagValue() {
        return this.value;
    }

    public void setFlagValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    public CommandSender getCause() {
        return cause;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
