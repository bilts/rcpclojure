(ns rcpclojure.constants
  (:import
    (org.eclipse.ui IWorkbenchActionConstants)))

(def 
  #^{:doc "The strings identifying the application's plugins"}
  plugins
  {:main "rcpclojure"})

(def 
  #^{:doc "The strings identifying the application's perspectives"}
  perspectives
  {:main "rcpclojure.perspective"})

(def 
  #^{:doc "The strings identifying the application's commands"}
  commands 
  {:open "rcpclojure.open"
   :open-message "rcpclojure.openMessage"})

(def 
  #^{:doc "The strings identifying the application's views"}
  views
  {:message "rcpclojure.view"
   :navigation "rcpclojure.navigationView"})

(def 
  #^{:doc "The application's menu information, including unique IDs and labels"}
  menus 
  {:file {:label "&File" :id IWorkbenchActionConstants/M_FILE}
   :additions {:group? true :id IWorkbenchActionConstants/MB_ADDITIONS}
   :help {:label "&Help" :id IWorkbenchActionConstants/M_HELP}})

(def 
  #^{:doc "The application's toolbar information, including unique IDs and labels"}
  toolbars 
  {:main {:id "main"}})