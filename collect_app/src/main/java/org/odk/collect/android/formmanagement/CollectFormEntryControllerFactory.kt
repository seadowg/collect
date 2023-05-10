package org.odk.collect.android.formmanagement

import org.javarosa.core.model.FormDef
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.core.model.instance.TreeReferenceIndex
import org.javarosa.entities.EntityFormFinalizationProcessor
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.odk.collect.android.tasks.FormLoaderTask.FormEntryControllerFactory
import org.odk.collect.forms.Form
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

class CollectFormEntryControllerFactory(
    private val settings: Settings,
    private val form: Form
) :
    FormEntryControllerFactory {
    override fun create(formDef: FormDef): FormEntryController {
        return FormEntryController(FormEntryModel(formDef)).also {
            it.addPostProcessor(EntityFormFinalizationProcessor())

            val index = indexMap.getOrPut(form.dbId) { InMemTreeReferenceIndex() }
            it.setTreeReferenceIndex(index)

            if (!settings.getBoolean(ProjectKeys.KEY_PREDICATE_CACHING)) {
                it.disablePredicateCaching()
            }
        }
    }
}

private val indexMap = mutableMapOf<Long, InMemTreeReferenceIndex>()

private class InMemTreeReferenceIndex : TreeReferenceIndex {

    private val map: MutableMap<String, MutableMap<String, MutableList<TreeReference>>> = mutableMapOf()

    override fun contains(section: String): Boolean {
        return map.containsKey(section)
    }

    override fun add(section: String, key: String, reference: TreeReference) {
        if (!map.containsKey(section)) {
            map[section] = HashMap()
        }

        val sectionMap: MutableMap<String, MutableList<TreeReference>>? = map[section]
        if (!sectionMap!!.containsKey(key)) {
            sectionMap[key] = ArrayList()
        }

        sectionMap[key]!!.add(reference)
    }

    override fun lookup(section: String, key: String): List<TreeReference> {
        return if (map.containsKey(section) && map[section]!!.containsKey(key)) {
            map[section]!![key]!!
        } else {
            emptyList()
        }
    }
}
