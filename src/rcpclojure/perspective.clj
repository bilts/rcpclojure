(ns rcpclojure.perspective
  (:gen-class
    :name "rcpclojure.Perspective"
    :implements (org.eclipse.ui.IPerspectiveFactory))
  (:use
    [rcpclojure.constants :only [views]])
  (:import
    (org.eclipse.ui IPageLayout IPerspectiveFactory)))

(defn -createInitialLayout
  "Creates the initial layout of the application perspective"
  [this layout]
  (let [editor-area (.getEditorArea layout)]
    (.setEditorAreaVisible layout false)
    (.addStandaloneView layout (:navigation views) false IPageLayout/LEFT 0.25 editor-area)
    (let [folder (.createFolder layout "messages" IPageLayout/TOP 0.5 editor-area)]
      (.addPlaceholder folder (str (:message views) ":*"))
      (.addView folder (:message views)))))
