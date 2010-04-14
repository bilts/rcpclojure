; Starts, stops, and provides access to the plugin
(ns rcpclojure.activator
  (:gen-class
    :name "rcpclojure.Activator"
    :extends org.eclipse.ui.plugin.AbstractUIPlugin
    :exposes-methods {start superStart stop superStop} )
  (:require 
    [rcpclojure.constants :as constants])  
  (:import
    (org.eclipse.jface.resource ImageDescriptor)
    (org.eclipse.ui.plugin AbstractUIPlugin)
    (org.osgi.framework.BundleContext)))

(def 
  #^{:doc "The currently running plugin"} 
  plugin (atom nil))

(defn -start 
  "Starts the plugin"
  [this context]
  (.superStart this context)
  (reset! plugin this))

(defn -stop
  "Stops the plugin"
  [this context]
  (reset! plugin nil)
  (.superStop this context))
