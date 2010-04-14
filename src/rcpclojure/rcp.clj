(ns rcpclojure.rcp
  (:require
    rcpclojure.constants
    rcpclojure.activator)
  (:use
    rcpclojure.interop) 
  (:import
    (org.eclipse.core.runtime Status)
    (org.eclipse.jface.dialogs MessageDialog)
    (org.eclipse.ui.plugin AbstractUIPlugin)
    (org.eclipse.jface.action Action)))

(defn log-error
  "Adds the message to the Eclipse RCP error log"
  ([message]
    (log-error message nil))
  ([message e]
    (let [status (Status. Status/ERROR (:main rcpclojure.constants/plugins) Status/ERROR (str message) e)]
      (.log (.getLog @rcpclojure.activator/plugin) status))))

(defn get-image-descriptor
  "Returns the image descriptor for the given plugin-relative path"
  [path]
  (AbstractUIPlugin/imageDescriptorFromPlugin (:main rcpclojure.constants/plugins) path))

(defn set-action-props
  "Sets properties on the action according to the values in the props map"
  [action props]
  (set-props! action props
    [setId :id]
    [setActionDefinitionId :id]
    [setText :text]
    [setImageDescriptor :image get-image-descriptor]))

(defmacro as-action
  "Executes the body expressions as an Eclipse RCP action with the given properties"
  [props & body]
  `(doto (proxy [Action] [] (run [] ~@body))
     (set-action-props ~props)))
