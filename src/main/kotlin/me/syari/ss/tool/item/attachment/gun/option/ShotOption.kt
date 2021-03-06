package me.syari.ss.tool.item.attachment.gun.option

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.core.message.Message.action
import me.syari.ss.core.particle.CustomParticleList
import me.syari.ss.core.persistentData.customType.PersistentDataTypeUUID
import me.syari.ss.core.scheduler.CustomScheduler.runLater
import me.syari.ss.core.sound.CustomSoundList
import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.attachment.gun.GunAttachment
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import java.util.*

data class ShotOption(
    val scope: Boolean,
    val between: Int,
    val particleList: CustomParticleList?,
    val sound: CustomSoundList?,
    val potion: List<PotionEffect>?
) {
    fun canShoot(player: Player, isScope: Boolean, item: CustomItemStack): Boolean {
        if (!isScope && scope) {
            player.action(GunAttachment.Companion.Message.NoScope.message)
            return false
        }
        val lastShot = getLastShot(item) ?: return true
        return if (shotHistory.contains(lastShot)) {
            false
        } else {
            setLastShot(item, null)
            true
        }
    }

    fun shoot(player: Player, item: CustomItemStack){
        val uuid = UUID.randomUUID()
        setLastShot(item, uuid)
        shotHistory.add(uuid)
        particleList?.spawn(player)
        sound?.play(player)
        if (potion != null) {
            player.addPotionEffects(potion)
        }
        runLater(toolPlugin, between.toLong()) {
            setLastShot(item, null)
            shotHistory.remove(uuid)
        }
    }

    companion object {
        private val default = ShotOption(
            false,
            0,
            null,
            null,
            null
        )

        fun getShotOption(config: CustomConfig, section: String): ShotOption {
            if (!config.contains(section)) return default
            return with(default){
                ShotOption(
                    config.get("$section.scope", ConfigDataType.BOOLEAN, scope, false),
                    config.get("$section.between", ConfigDataType.INT, between, false),
                    config.get("$section.particle", ConfigDataType.PARTICLE, false),
                    config.get("$section.sound", ConfigDataType.SOUND, false),
                    config.get("$section.potion", ConfigDataType.POTION, false)
                )
            }
        }

        private val shotHistory = mutableSetOf<UUID>()

        private const val gunLastShotPersistentKey = "ss-tool-gun-last-shot"

        fun getLastShot(item: CustomItemStack): UUID? {
            return item.getPersistentData(toolPlugin)?.get(gunLastShotPersistentKey, PersistentDataTypeUUID)
        }

        fun setLastShot(item: CustomItemStack, uuid: UUID?) {
            item.editPersistentData(toolPlugin) {
                set(gunLastShotPersistentKey, PersistentDataTypeUUID, uuid)
            }
        }
    }
}