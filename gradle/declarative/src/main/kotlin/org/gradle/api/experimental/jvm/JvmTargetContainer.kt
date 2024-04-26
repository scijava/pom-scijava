package org.gradle.api.experimental.jvm

import org.gradle.api.Action
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.internal.DefaultNamedDomainObjectSet
import org.gradle.declarative.dsl.model.annotations.Adding
import org.gradle.declarative.dsl.model.annotations.Restricted
import org.gradle.internal.instantiation.InstantiatorFactory
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.service.ServiceRegistry
import javax.inject.Inject

@Restricted
class JvmTargetContainer
@Inject
constructor(instantiator: Instantiator,
            instantiatorFactory: InstantiatorFactory,
            serviceRegistry: ServiceRegistry, callbackDecorator: CollectionCallbackActionDecorator)
    : DefaultNamedDomainObjectSet<JvmTarget>(JvmTarget::class.java, instantiator, callbackDecorator) {

    private val elementInstantiator: Instantiator = instantiatorFactory.decorateLenient(serviceRegistry)

    @Adding
    fun java(version: Int): JvmTarget = java(version) {}

    @Adding
    fun java(version: Int, action: Action<in JvmTarget>): JvmTarget {
        val element = elementInstantiator.newInstance(JavaTarget::class.java, version)
        add(element)
        action.execute(element)
        return element
    }
}
