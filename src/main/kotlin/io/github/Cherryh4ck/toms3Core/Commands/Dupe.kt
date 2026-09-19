package io.github.Cherryh4ck.toms3Core.Commands

import io.github.Cherryh4ck.toms3Core.DiscordWebhook
import io.github.Cherryh4ck.toms3Core.Toms3Core
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File

class Dupe(private val plugin : Toms3Core) : TabExecutor {
    val minimessage = MiniMessage.miniMessage()
    val drunkness = PotionEffect(PotionEffectType.NAUSEA, 30 * 20, 0, false, true, true)
    val darkness = PotionEffect(PotionEffectType.DARKNESS, 60 * 20, 0, false, true, true)
    val poison = PotionEffect(PotionEffectType.POISON, 120 * 20, 2, false, true, true)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            val locale = sender.locale().toString()
            val isSpanish = locale.startsWith("es") // le isspanish
            val isJapanese = locale.startsWith("ja")
            val playerDataCache = File(plugin.playerDataPath, "${sender.name.lowercase()}.yml")
            val config = YamlConfiguration.loadConfiguration(playerDataCache)
            val alreadyFallen = config.getBoolean("fallen-for-dupe")

            if (alreadyFallen) {
                if (isSpanish && plugin.spanish_enabled) {
                    sender.sendMessage(minimessage.deserialize("${plugin.prefix} <red>No seas idiota, newfag.</red>"))
                }
                else if (isJapanese && plugin.japanese_enabled) {
                    sender.sendMessage(minimessage.deserialize("${plugin.prefix} <red>バカなことすんなよ、newfag。</red>"))
                }
                else {
                    sender.sendMessage(minimessage.deserialize("${plugin.prefix} <red>Don't be an idiot, newfag.</red>"))
                }
                return true
            }

            if (plugin.discordWebhook.toString().isNotEmpty()){
                Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                    val message = plugin.dupe_webhook_message.toString().replace("%username%", sender.name)
                    DiscordWebhook.sendDiscordWebhook(plugin.discordWebhook.toString(), message)
                })
            }

            val joinMessage = if (isSpanish && plugin.spanish_enabled) { minimessage.deserialize("<gray>popbob se unió al servidor.</gray>") } else if (isJapanese && plugin.japanese_enabled) { minimessage.deserialize("<gray>popbobがサーバーに参加しました。</gray>") } else { minimessage.deserialize("<gray>popbob has joined the server.</gray>") }
            val hallOfShameMessage = if (isSpanish && plugin.spanish_enabled) { minimessage.deserialize("<red>Ahora estás en la lista de jugadores lamentables.</red>") } else if (isJapanese && plugin.japanese_enabled) { minimessage.deserialize("<red>あなたは恥の殿堂入りしました。</red>") } else { minimessage.deserialize("<red>You have been added to the Hall of Shame.</red>") }
            sender.sendMessage(joinMessage)
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                val coordsMessage = if (isSpanish && plugin.spanish_enabled) {
                    "${sender.name} » popbob mis coordenadas son ${sender.x.toInt()} ${sender.z.toInt()} ven a buscarme porfa"
                } else if (isJapanese && plugin.japanese_enabled) {
                    "${sender.name} » popbob 座標は${sender.x.toInt()} ${sender.z.toInt()}だよ、助けに来て"
                } else {
                    "${sender.name} » popbob my coords are ${sender.x.toInt()} ${sender.z.toInt()} come get me please"
                }
                sender.sendMessage(coordsMessage)
            }, 20L)
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                val comingMessage = if (isSpanish && plugin.spanish_enabled) {
                    "popbob » estás jodido, voy para allá"
                } else if (isJapanese && plugin.japanese_enabled) {
                    "popbob » お前終わったな、今行くぞ"
                } else {
                    "popbob » ur fucked, im coming"
                }
                sender.sendMessage(comingMessage)
                val health = sender.health - 1.0
                sender.damage(health)
                sender.addPotionEffect(drunkness)
                sender.addPotionEffect(darkness)
                sender.addPotionEffect(poison)
                sender.playSound(sender.location, Sound.AMBIENT_CAVE, 2.0f, 0.5f)
                sender.playSound(sender.location, Sound.ITEM_TOTEM_USE, 2.0f, 0.5f)
            }, 50L)
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                val world = Bukkit.getWorld(sender.world.name)
                val location = Location(world, sender.location.x, sender.location.y, sender.location.z)
                world?.strikeLightning(location)
                sender.sendMessage(hallOfShameMessage)
                sender.playSound(sender.location, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.15f)
                Bukkit.getOnlinePlayers()
                    .filter { it != sender }
                    .forEach { player ->
                    val locale = player.locale().toString()
                    if (locale.startsWith("es") && plugin.spanish_enabled) {
                        player.sendMessage(minimessage.deserialize("<gray>${sender.name} fue agregado a la <light_purple>lista de jugadores miserables</light_purple>."))
                    }
                    else if (locale.startsWith("ja") && plugin.japanese_enabled) {
                        player.sendMessage(minimessage.deserialize("<gray>${sender.name}が<light_purple>恥の殿堂</light_purple>に追加されました。"))
                    }
                    else{
                        player.sendMessage(minimessage.deserialize("<gray>${sender.name} has been added to the <light_purple>Hall of Shame</light_purple>."))
                    }
                }
            }, 100L)

            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                config.set("fallen-for-dupe", true)
                try {
                    config.save(playerDataCache)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            })
        }
        else{
            plugin.logToConsole("<red>You must be a player to execute this command.")
        }
        return true
    }

    override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, p3: Array<out String> ): List<String?>? {
        return emptyList()
    }
}