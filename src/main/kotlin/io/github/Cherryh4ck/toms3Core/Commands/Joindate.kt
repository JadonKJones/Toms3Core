package io.github.Cherryh4ck.toms3Core.Commands

import io.github.Cherryh4ck.toms3Core.Toms3Core
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class Joindate(private val plugin: Toms3Core) : TabExecutor {
    val minimessage = MiniMessage.miniMessage()

    fun validateUsername(username: String): Boolean {
        if (!plugin.usernameValidationRegexEnabled) {
            val regex = Regex(plugin.usernameValidationRegex)
            return regex.matches(username)
        }
        else {
            return true
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val targetUser : String
        val userLocale : String
        val isSpanish : Boolean
        val isJapanese : Boolean

        if (sender is Player){
            userLocale = sender.locale().toString()
            isSpanish = userLocale.startsWith("es")
            isJapanese = userLocale.startsWith("ja")

            targetUser = if (args.isEmpty()) {
                sender.name
            } else{
                args[0]
            }
        }
        else{
            isSpanish = true
            isJapanese = false
            if (args.isEmpty()){
                plugin.logToConsole("<red>You need to specify a player to use this command.")
                return true
            }
            else {
                targetUser = args[0]
            }
        }

        if (!validateUsername(targetUser)) {
            val mensaje = if (isSpanish && plugin.spanish_enabled) {
                minimessage.deserialize("${plugin.prefix} <red>$targetUser no es un nombre de jugador válido.</red>")
            }
            else if (isJapanese && plugin.japanese_enabled) {
                minimessage.deserialize("${plugin.prefix} <red>$targetUser は有効なプレイヤー名ではありません。</red>")
            }
            else {
                minimessage.deserialize("${plugin.prefix} <red>$targetUser is not a valid player name.</red>")
            }
            sender.sendMessage(mensaje)
            return true
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val playerDataCache = File(plugin.playerDataPath, "${targetUser.lowercase()}.yml")
            val offlineplayer = if (!playerDataCache.exists()) {
                plugin.logToConsole("<red>$targetUser's UUID file doesn't exist.")
                Bukkit.getOfflinePlayer(targetUser)
            }
            else{
                val config = YamlConfiguration.loadConfiguration(playerDataCache)
                val configUuid = config.getString("uuid")
                if (configUuid == null) {
                    plugin.logToConsole("<yellow>UUID value is null.")
                    Bukkit.getOfflinePlayer(targetUser)
                }
                else{
                    val uuid = java.util.UUID.fromString(configUuid)
                    plugin.logToConsole("<green>UUID file found: ${uuid.toString()}")
                    Bukkit.getOfflinePlayer(uuid)
                }
            }

            if (!offlineplayer.isOnline && offlineplayer.firstPlayed == 0L) {
                val message = if (isSpanish && plugin.spanish_enabled){
                    minimessage.deserialize("${plugin.prefix} <red>${offlineplayer.name} nunca entró al servidor o no está en el cache del servidor.</red>")
                }
                else if (isJapanese && plugin.japanese_enabled){
                    minimessage.deserialize("${plugin.prefix} <red>${offlineplayer.name} はサーバーに参加したことがないか、サーバーのキャッシュにありません。</red>")
                }
                else{
                    minimessage.deserialize("${plugin.prefix} <red>${offlineplayer.name} has never entered the server or is not in the server cache.</red>")
                }

                sender.sendMessage(message)
                return@Runnable
            }

            val unixTime = offlineplayer.firstPlayed
            val format = if (isSpanish && plugin.spanish_enabled) {
                SimpleDateFormat("dd/MM/yyyy HH:mm")
            } else if (isJapanese && plugin.japanese_enabled) {
                SimpleDateFormat("yyyy/MM/dd HH:mm")
            } else {
                SimpleDateFormat("MM/dd/yyyy hh:mm a")
            }
            val result = format.format(Date(unixTime))

            val message = if (isSpanish && plugin.spanish_enabled){
                if (offlineplayer.name != sender.name){
                    minimessage.deserialize("<gold>${plugin.prefix} <bold>${offlineplayer.name}</bold> se unió al servidor el <bold>${result}</bold>.</gold>")
                }
                else{
                    minimessage.deserialize("<gold>${plugin.prefix} Te uniste al servidor el <bold>${result}</bold>.</gold>")
                }
            }
            else if (isJapanese && plugin.japanese_enabled){
                if (offlineplayer.name != sender.name){
                    minimessage.deserialize("<gold>${plugin.prefix} <bold>${offlineplayer.name}</bold> は <bold>${result}</bold> にサーバーへ参加しました。</gold>")
                }
                else{
                    minimessage.deserialize("<gold>${plugin.prefix} あなたは <bold>${result}</bold> にサーバーへ参加しました。</gold>")
                }
            }
            else{
                if (offlineplayer.name != sender.name){
                    minimessage.deserialize("<gold>${plugin.prefix} <bold>${offlineplayer.name}</bold> joined the server on <bold>${result}</bold>.</gold>")
                }
                else{
                    minimessage.deserialize("<gold>${plugin.prefix} You joined the server on <bold>${result}</bold>.</gold>")
                }
            }

            sender.sendMessage(message)
        })
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        return if (args.size == 1){
            Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter { it.startsWith(args[0], ignoreCase = true) }
        }
        else{
            emptyList()
        }
    }
}