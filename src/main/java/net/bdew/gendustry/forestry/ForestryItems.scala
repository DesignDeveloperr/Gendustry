/*
 * Copyright (c) bdew, 2013 - 2014
 * https://github.com/bdew/gendustry
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.gendustry.forestry

import cpw.mods.fml.common.registry.GameRegistry

object ForestryItems {
  lazy val honeydew = GameRegistry.findItem("Forestry", "honeydew")
  lazy val canEmpty = GameRegistry.findItem("Forestry", "canEmpty")
}

