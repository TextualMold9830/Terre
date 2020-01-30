/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.plugin

import kotlinx.coroutines.CoroutineDispatcher
import org.apache.logging.log4j.Logger
import org.lanternpowered.terre.Console
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.event.EventBus
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.plugin.PluginManager
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Injects objects.
 */
@PublishedApi
internal fun Any?.inject(type: KType): Any? {
  val kClass = type.classifier as KClass<*>

  val value: Any? = when (kClass) {
    EventBus::class -> EventBus
    Proxy::class -> Proxy
    PluginManager::class -> PluginManager
    Console::class -> Console
    CoroutineDispatcher::class -> Proxy.dispatcher
    else -> {
      val pluginContainer = if (this != null) {
        ProxyImpl.pluginManager.getPluginContainer(this)
      } else null ?: Thread.currentThread().activePlugin as? TerrePluginContainer
      pluginContainer?.inject(type)
    }
  }

  if (value == null && !type.isMarkedNullable) {
    error("Cannot retrieve ${kClass.qualifiedName} within the current context.")
  }

  return value
}

/**
 * Injects plugin related objects.
 */
private fun TerrePluginContainer.inject(type: KType): Any? {
  return when (type.classifier as KClass<*>) {
    PluginContainer::class -> this
    Logger::class -> this.logger
    java.util.logging.Logger::class -> this.javaLogger
    else -> null
  }
}
