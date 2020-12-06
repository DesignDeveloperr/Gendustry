/*
 * Copyright (c) bdew, 2013 - 2014
 * https://github.com/bdew/gendustry
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.gendustry.nei

import codechicken.lib.gui.GuiDraw
import net.bdew.lib.gui.SimpleDrawTarget
import net.minecraft.client.Minecraft

object NEIDrawTarget extends SimpleDrawTarget {
  def getZLevel = GuiDraw.gui.getZLevel
  def getFontRenderer = Minecraft.getMinecraft.fontRenderer
}
