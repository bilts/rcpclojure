; Sets up the overall application window, menus, toolbars, and actions
(ns rcpclojure.application
  (:gen-class 
    :name "rcpclojure.Application"
    :implements (org.eclipse.equinox.app.IApplication))
  (:require 
    rcpclojure.activator)
  (:use 
    rcpclojure.interop
    rcpclojure.rcp
    rcpclojure.constants
    rcpclojure.widgets)
  (:import 
    (org.eclipse.swt SWT)
    (org.eclipse.swt.graphics Point)
    (org.eclipse.equinox.app IApplication)
    (org.eclipse.ui PlatformUI IWorkbenchPage IWorkbenchWindow PartInitException)
    (org.eclipse.ui.actions ActionFactory)
    (org.eclipse.ui.application 
      WorkbenchAdvisor WorkbenchWindowAdvisor IWorkbenchWindowConfigurer
      ActionBarAdvisor IActionBarConfigurer)
    (org.eclipse.jface.action IAction GroupMarker MenuManager Separator ToolBarContributionItem ToolBarManager)))

(defn open-view [window id instance-id]
  "Given a window, the ID of a view, and a unique instance ID for that view,
    opens the view with the corresponding ID in the window."
  (when window
    (try
      (.. window getActivePage (showView id (str instance-id) IWorkbenchPage/VIEW_ACTIVATE))
      (catch PartInitException e
        (let [message (str "Error opening view: " (.getMessage e))] 
          (show-error window message e :id instance-id :view-id id))))))

(defn open-message-popup-action [window]
  "Creates an action which pops up a window"
  (as-action {:id (:open-message commands)
              :text "Open Message"
              :image "/icons/sample3.gif"}
    (show-info (.getShell window) "Open" "Open Message")))

(defn open-message-view-action
  "Creates an action which opens a new view containing a message"
  [window]
  (let [instance-id (atom 0)] ;Unique ID for the view instance
    (as-action {:id (:open commands)
                :text "Open Another Message View"
                :image "/icons/sample2.gif"}
      (open-view window (:message views) (swap! instance-id inc)))))

(defmacro eclipse-action 
  "Creates a built-in eclipse action for the given factory.  
   See org.eclipse.ui.actions.ActionFactory for valid factory IDs"
  [factory window]
  `(.create (. ActionFactory ~factory) ~window))

(defn make-actions 
  "Given a constructed window, creates all of the actions for the plugin"
  [window]
  {:new-window (eclipse-action OPEN_NEW_WINDOW window)
   :open-view (open-message-view-action window)
   :open-message (open-message-popup-action window)
   :exit (eclipse-action QUIT window)
   :about (eclipse-action ABOUT window)
   :separator (Separator.)})

(defn menu
  "Given a map containing menu info (described below), a map of actions, and a
   list of keys, adds actions corresponding to the keys to the menu with the 
   given ID.

   Menu is a map containing information about a menu:
   :id (required) holds the unique string identifying the menu
   :label holds the user-visible string for the menu, e.g. &File
   :group? is true if the menu marks a logical group of menus
   "
  ([info] (menu info {} []))
  ([info action-map actions]
    (if (:group? info)
      (apply add-keys! (GroupMarker. (:id info)) action-map actions)
      (apply add-keys! (MenuManager. (:label info) (:id info)) action-map actions))))

(defn toolbar
  "Given a map containing at least {:id toolbar-id}, a map of actions, and a 
   list of keys, adds actions corresponding to the keys to the toolbar with the
   given ID." 
  [info action-map actions]
  (let [manager (ToolBarManager. (bit-or SWT/FLAT SWT/RIGHT))]
    (apply add-keys! manager action-map actions)
    (ToolBarContributionItem. manager (:id info))))

(defn make-actionbar-advisor 
  "Make an action bar advisor, responsible for configuring the menus and toolbars"
  [configurer]
  (let [actions (promise)] 
    (proxy [ActionBarAdvisor] [configurer]
      
      (makeActions [window]
        ; Create actions for the given window
        (deliver actions (make-actions window))
        (doseq [action (filter #(isa? (class %) IAction) (keys @actions))] 
          (.register this action)))
      
      (fillMenuBar [menu-bar]
        ; Fill the application menu bar with actions
        (add! menu-bar
          (menu (:file menus) @actions [:new-window
                                        :separator
                                        :open-view
                                        :open-message
                                        :separator
                                        :exit])
          (menu (:additions menus))
          (menu (:help menus) @actions [:about])))
      
      (fillCoolBar [cool-bar]
        ; Fill the application cool bar (toolbar) with actions
        (add! cool-bar
          (toolbar (:main toolbars) @actions [:open-view
                                              :open-message]))))))

(defn make-window-advisor 
  "Make a window advisor, responsible for configuring the main window"
  [configurer]
  (proxy [WorkbenchWindowAdvisor] [configurer]
    (createActionBarAdvisor [configurer]
      (make-actionbar-advisor configurer))
    (preWindowOpen []
      (doto (.getWindowConfigurer this)
        (.setInitialSize (Point. 800 600))
        (.setShowCoolBar true)
        (.setShowStatusLine false)))))

(def 
  #^{:doc "The workbench advisor, responsible for configuring the workbench"}
  advisor
  (proxy [WorkbenchAdvisor] []
    (createWorkbenchWindowAdvisor [configurer]
      (make-window-advisor configurer))
    (getInitialWindowPerspectiveId []
      (:main perspectives))))

(defn -start 
  "Start the application, creating the workbench"
  [this context]
  (with-disposable [display (PlatformUI/createDisplay)]
    (if (= PlatformUI/RETURN_RESTART
          (PlatformUI/createAndRunWorkbench display advisor))
      IApplication/EXIT_RESTART
      IApplication/EXIT_OK)))

(defn -stop 
  "Stop the application, closing the workbench"
  [this context]
  (when-let [workbench (PlatformUI/getWorkbench)]
    (.. workbench getDisplay 
      (syncExec #(if-not (.. workbench getDisplay isDisposed) 
                   (.close workbench))))))


