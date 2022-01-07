package helpers

import com.sksamuel.hoplite.ConfigLoader

object ConfigChooser {

    fun getConfig1m(instanceId: Int): Config {
        return getConfig(instanceId, "1m")
    }

    fun getConfig5m(instanceId: Int): Config {
        return getConfig(instanceId, "5m")
    }

    fun getConfigUnbounded(instanceId: Int): Config {
        return getConfig(instanceId, "un")
    }

    fun getConfig(instanceId: Int, timeLimitMarker: String): Config {
        return getConfigFromResource("/config-$timeLimitMarker-i$instanceId.yaml")
    }

    private fun getConfigFromResource(resource: String): Config {
        return ConfigLoader().loadConfigOrThrow(resource)
    }

}
