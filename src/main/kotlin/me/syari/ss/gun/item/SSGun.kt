package me.syari.ss.gun.item

import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.gun.Main.Companion.gunPlugin
import me.syari.ss.gun.item.attachment.GunAction
import me.syari.ss.gun.item.attachment.base.Attachment
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SSGun(
    val id: String,
    val type: Material,
    val name: String,
    val lore: List<String>,
    val maxDurability: Int,
    val attachments: Map<GunAction, Attachment>
) {
    fun create() = SSGunItem(CustomItemStack.create(type, name, lore).apply {
        editPersistentData(gunPlugin) {
            set("ss-gun-id", PersistentDataType.STRING, id)
        }
    }, this)

    companion object {
        private val gunList = mutableMapOf<String, SSGun>()

        fun from(item: ItemStack?): SSGun? {
            return item?.let { from(CustomItemStack.create(item)) }
        }

        fun from(item: CustomItemStack?): SSGun? {
            return item?.let { get(item.getPersistentData(gunPlugin)?.get("ss-gun-id", PersistentDataType.STRING)) }
        }

        fun get(id: String?): SSGun? {
            return gunList[id]
        }

        fun register(
            id: String,
            type: Material,
            name: String,
            lore: List<String>,
            maxDurability: Int,
            attachments: Map<GunAction, Attachment>
        ) {
            val gun = SSGun(id, type, name, lore, maxDurability, attachments)
            gunList[id] = gun
        }

        fun clearAll(){
            gunList.clear()
        }
    }
}