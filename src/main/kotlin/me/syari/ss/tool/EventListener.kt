package me.syari.ss.tool

import me.syari.ss.core.auto.Event
import me.syari.ss.core.message.Message.action
import me.syari.ss.tool.item.SSTool
import me.syari.ss.tool.item.attachment.ClickType
import me.syari.ss.tool.item.attachment.gun.GunAttachment
import me.syari.ss.tool.item.attachment.gun.option.ReloadOption
import me.syari.ss.tool.item.attachment.gun.option.ScopeOption
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.*

object EventListener : Event {
    @EventHandler
    fun onUseGun(e: PlayerInteractEvent) {
        val action = e.action
        if (action == Action.PHYSICAL) return
        val ssToolItem = SSTool.from(e.item) ?: return
        e.isCancelled = true
        val player = e.player
        if (ssToolItem.durability < 1) return player.action(GunAttachment.Companion.Message.BrokenGun.message)
        val clickType = ClickType.from(action) ?: return
        ssToolItem.data.gunAttachments[clickType]?.let {
            val attachmentCursor = GunAttachment.getCursor(ssToolItem)
            if (attachmentCursor == clickType) {
                val isSuccess = it.shoot(player, clickType, ssToolItem)
                if (isSuccess) {
                    ssToolItem.durability -= it.wearOut
                    ssToolItem.updateDurability()
                }
            } else if (player.isSneaking) {
                if (attachmentCursor != null) it.scope(player)
            } else {
                ReloadOption.cancelReload(player)
                GunAttachment.setCursor(ssToolItem, clickType)
                ssToolItem.updateDisplayName()
            }
        }
        ssToolItem.data.shieldAttachments[clickType]?.let {

        }
    }

    @EventHandler
    fun onGunReload(e: PlayerDropItemEvent) {
        val ssToolItem = SSTool.from(e.itemDrop.itemStack) ?: return
        e.isCancelled = true
        val cursor = GunAttachment.getCursor(ssToolItem) ?: return
        val attachment = ssToolItem.data.gunAttachments[cursor] ?: return
        val player = e.player
        attachment.reload(player, cursor, ssToolItem)
    }

    @EventHandler
    fun onMeleeDamage(e: EntityDamageByEntityEvent) {
        val attacker = e.damager as? Player ?: return
        val victim = e.entity as? LivingEntity ?: return
        val ssToolItem = SSTool.from(attacker.inventory.itemInMainHand) ?: return
        ssToolItem.data.meleeAttachment?.let {
            it.damage(victim)
            ssToolItem.durability -= it.wearOut
            ssToolItem.updateDurability()
        }
    }

    @EventHandler
    fun onCancelGun(e: PlayerToggleSneakEvent) {
        val player = e.player
        if (!e.isSneaking && ScopeOption.isUseScope(player)) {
            ScopeOption.cancelScope(player)
        }
    }

    @EventHandler
    fun onCancelGun(e: PlayerItemHeldEvent) {
        val player = e.player
        ReloadOption.cancelReload(player)
        if (ScopeOption.isUseScope(player)) {
            ScopeOption.cancelScope(player)
        }
    }

    @EventHandler
    fun onCancelGun(e: PlayerQuitEvent) {
        val player = e.player
        ReloadOption.cancelReload(player)
        if (ScopeOption.isUseScope(player)) {
            ScopeOption.cancelScope(player)
        }
    }
}