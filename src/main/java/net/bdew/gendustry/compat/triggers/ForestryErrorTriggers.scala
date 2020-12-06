/*
 * Copyright (c) bdew, 2013 - 2014
 * https://github.com/bdew/gendustry
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.gendustry.compat.triggers

import buildcraft.api.statements.StatementManager
import cpw.mods.fml.relauncher.{Side, SideOnly}
import forestry.api.core.{ForestryAPI, IErrorLogicSource, IErrorState}
import net.bdew.gendustry.Gendustry
import net.bdew.lib.Misc
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraftforge.common.util.ForgeDirection

case class ForestryErrorTrigger(state: IErrorState) extends BaseTrigger("forestry.error." + state.getID, "y%03d".format(state.getID), classOf[IErrorLogicSource]) {
  override def getIcon = state.getIcon
  override def getDescription = Misc.toLocal("for." + state.getDescription)
  override def registerIcons(ir: IIconRegister) {}
  override def getState(side: ForgeDirection, tile: IErrorLogicSource) =
    tile.getErrorLogic.contains(state)
}

object ForestryNoErrorTrigger extends BaseTrigger("forestry.noerror", "_", classOf[IErrorLogicSource]) {
  override def getDescription = Misc.toLocal("gendustry.errorstate.ok")

  @SideOnly(Side.CLIENT)
  override def registerIcons(ir: IIconRegister): Unit = {
    icon = ir.registerIcon("gendustry:error/ok")
  }

  override def getState(side: ForgeDirection, tile: IErrorLogicSource) =
    !tile.getErrorLogic.hasErrors
}

object ForestryErrorTriggers {

  import scala.collection.JavaConversions._

  val errorStateRegistry = ForestryAPI.errorStateRegistry

  val triggers = errorStateRegistry.getErrorStates.map(ForestryErrorTrigger) ++ List(ForestryNoErrorTrigger)

  def register() {
    triggers.foreach(StatementManager.registerStatement)
    StatementManager.registerStatement(ForestryNoErrorTrigger)
    Gendustry.logInfo("Created %d BC triggers for Forestry error codes", triggers.size)
  }
}
