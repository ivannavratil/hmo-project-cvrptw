package helpers

import com.sksamuel.hoplite.ConfigLoader

object ConfigChooser {

    fun getConfig1m(instanceId: Int): Config {
        return getConfigFromResource("/config-1m-i$instanceId.yaml")
    }

    fun getConfig5m(instanceId: Int): Config {
        return getConfigFromResource("/config-5m-i$instanceId.yaml")
    }

    fun getConfigUnbounded(instanceId: Int): Config {
        return getConfigFromResource("/config-un-i$instanceId.yaml")
    }

    private fun getConfigFromResource(resource: String): Config {
        return ConfigLoader().loadConfigOrThrow(resource)
    }

}
