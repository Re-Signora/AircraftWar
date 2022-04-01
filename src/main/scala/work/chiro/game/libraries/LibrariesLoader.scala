package work.chiro.game.libraries

import org.luaj.vm2.Globals
import work.chiro.game.application.Main
import work.chiro.game.application.Main.getLuaGlobals

/**
 * 加载 Lua 中用到的所有的库
 */
object LibrariesLoader {
  def get = Array(
    () => new LuaGame,
    () => new HyperbolicTest,
    () => new LuaLogging
  )

  def loadAllLibraries(globals: Globals = Main.getLuaGlobals): Unit = {
    get.foreach(getLib => globals.load(getLib()))
    // 加载 lua 语言的自定义库
    getLuaGlobals.loadfile("scripts/lib/logger.lua").call()
  }
}
