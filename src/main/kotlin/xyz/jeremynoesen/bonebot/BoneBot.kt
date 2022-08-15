package xyz.jeremynoesen.bonebot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import xyz.jeremynoesen.bonebot.modules.Statuses
import java.awt.Toolkit
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.PrintStream
import javax.imageio.ImageIO


/**
 * Main class, initializes bot, loads all data, and initializes modules
 *
 * @author Jeremy Noesen
 */
object BoneBot {

    /**
     * instance of JDA for this bot
     */
    var JDA: JDA? = null

    /**
     * create the bot and run it
     *
     * @param args program arguments (not used)
     */
    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty("apple.awt.UIElement", "true") //hide dock icon on macOS
        Toolkit.getDefaultToolkit()
        ImageIO.setUseCache(false)
        Config.loadData()
        JDA = JDABuilder.createLight(Config.botToken).addEventListeners(Listener())
            .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT).build()
        if (Statuses.enabled) Statuses.setStatus()
    }
}