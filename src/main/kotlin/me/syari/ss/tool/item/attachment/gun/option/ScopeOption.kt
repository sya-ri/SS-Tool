package me.syari.ss.tool.item.attachment.gun.option

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.tool.Main.Companion.toolPlugin
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

data class ScopeOption(
    val maxAmount: Int,
    val nightVision: Boolean
) {
    fun scope(player: Player) {
        if (isUseScope(player)) {
            cancelScope(player)
        } else {
            val speedEffect = player.getPotionEffect(PotionEffectType.SPEED)
            if (speedEffect != null) {
                if (!player.hasMetadata(lastSpeedMetaDataKey)) {
                    player.setMetadata(lastSpeedMetaDataKey, FixedMetadataValue(toolPlugin, speedEffect))
                }
                player.removePotionEffect(PotionEffectType.SPEED)
            }
            player.setMetadata(zoomMetaDataKey, FixedMetadataValue(toolPlugin, true))
            PotionEffectType.SPEED.createEffect(2400, -maxAmount).apply(player)
            if (nightVision && !player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.setMetadata(nightVisionMetaDataKey, FixedMetadataValue(toolPlugin, true))
                PotionEffectType.NIGHT_VISION.createEffect(2400, -1)
            }
        }
    }

    companion object {
        private val default = ScopeOption(
            0,
            true
        )

        fun getScopeOption(config: CustomConfig, section: String): ScopeOption {
            if (!config.contains(section)) return default
            return ScopeOption(
                config.get("$section.amount", ConfigDataType.INT, default.maxAmount, false),
                config.get("$section.night", ConfigDataType.BOOLEAN, default.nightVision, false)
            )
        }

        private const val zoomMetaDataKey = "ss-tool-gun-scope-zoom"
        private const val lastSpeedMetaDataKey = "ss-tool-gun-scope-speed"
        private const val nightVisionMetaDataKey = "ss-tool-gun-scope-night"

        fun isUseScope(player: Player): Boolean {
            return player.hasMetadata(zoomMetaDataKey)
        }

        fun getScopeAmount(player: Player): Int {
            return if (isUseScope(player)) (player.getMetadata(zoomMetaDataKey)[0]).asInt() else 0
        }

        fun cancelScope(player: Player) {
            player.removePotionEffect(PotionEffectType.SPEED)
            player.removeMetadata(zoomMetaDataKey, toolPlugin)
            if (player.hasMetadata(lastSpeedMetaDataKey)) {
                val speedEffect = player.getMetadata(lastSpeedMetaDataKey)[0].value() as? PotionEffect
                speedEffect?.apply(player)
                player.removeMetadata(lastSpeedMetaDataKey, toolPlugin)
            }
            if (player.hasMetadata(nightVisionMetaDataKey)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION)
                player.removeMetadata(nightVisionMetaDataKey, toolPlugin)
            }
        }
    }
}