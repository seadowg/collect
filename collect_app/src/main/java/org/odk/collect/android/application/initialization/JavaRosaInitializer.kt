package org.odk.collect.android.application.initialization

import org.javarosa.core.model.CoreModelModule
import org.javarosa.core.services.PrototypeManager
import org.javarosa.core.util.JavaRosaCoreModule
import org.javarosa.entities.EntityXFormParserFactory
import org.javarosa.model.xform.XFormsModule
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.parse.XFormParserFactory
import org.javarosa.xform.util.XFormUtils
import org.odk.collect.android.dynamicpreload.DynamicPreloadXFormParserFactory
import org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointActionHandler
import org.odk.collect.android.projects.ProjectDependencyProvider
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.entities.LocalEntitiesExternalInstanceParserFactory
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class JavaRosaInitializer(
    private val projectsDataService: ProjectsDataService,
    private val propertyManager: PropertyManager,
    private val entitiesRepositoryProvider: ProjectDependencyProvider<EntitiesRepository>,
    private val settingsProvider: SettingsProvider
) {

    fun initialize() {
        propertyManager.reload()
        org.javarosa.core.services.PropertyManager
            .setPropertyManager(propertyManager)

        // Register prototypes for classes that FormDef uses
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames)
        PrototypeManager.registerPrototypes(CoreModelModule.classNames)
        XFormsModule().registerModule()

        // When registering prototypes from Collect, a proguard exception also needs to be added
        PrototypeManager.registerPrototype("org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointAction")
        XFormParser.registerActionHandler(
            CollectSetGeopointActionHandler.ELEMENT_NAME,
            CollectSetGeopointActionHandler()
        )

        // Configure default parser factory
        val entityXFormParserFactory = EntityXFormParserFactory(XFormParserFactory())
        val dynamicPreloadXFormParserFactory =
            DynamicPreloadXFormParserFactory(entityXFormParserFactory)

        XFormUtils.setXFormParserFactory(dynamicPreloadXFormParserFactory)

        val localEntitiesExternalInstanceParserFactory = LocalEntitiesExternalInstanceParserFactory(
            { entitiesRepositoryProvider.get(projectsDataService.getCurrentProject().uuid) },
            { settingsProvider.getUnprotectedSettings().getBoolean(ProjectKeys.KEY_LOCAL_ENTITIES) }
        )

        XFormUtils.setExternalInstanceParserFactory(localEntitiesExternalInstanceParserFactory)
    }
}
