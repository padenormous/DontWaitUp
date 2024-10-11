package paden.modid

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import org.slf4j.LoggerFactory

object DontWaitUp : ModInitializer {
	private val logger = LoggerFactory.getLogger("dont-wait-up")

	override fun onInitialize() {
		logger.info("Initializing Don't Wait Up mod")

		ServerTickEvents.END_WORLD_TICK.register { world: ServerWorld ->
			val isNight = world.timeOfDay in 13000..23000
			if (isNight && world.players.isNotEmpty()) {
				val sleepingPlayer = world.players.find { it.isSleeping }
				if (sleepingPlayer != null) {
					skipNight(world, sleepingPlayer)
				}
			}
		}
	}

	private fun skipNight(world: ServerWorld, sleepingPlayer: ServerPlayerEntity) {
		// Set time to morning
		world.setTimeOfDay(1000)

		// Clear weather
		world.setWeather(0, 0, false, false)

		// Wake up all sleeping players
		world.players.forEach { player ->
			if (player.isSleeping) {
				player.wakeUp(true, true)
			}
		}

		// Broadcast message
		val message = Text.of("Good morning! ${sleepingPlayer.name.string} has slept to skip the night.")
		world.server.playerManager.broadcast(message, false)
	}
}