(ns rcpclojure.navigation-view
  (:gen-class
    :name "rcpclojure.NavigationView"
    :extends org.eclipse.ui.part.ViewPart)
  (:import
    (org.eclipse.ui ISharedImages PlatformUI)
    (org.eclipse.ui.part ViewPart)
    (org.eclipse.swt.widgets Composite)
    (org.eclipse.swt.graphics Image)
    (org.eclipse.swt SWT)
    (org.eclipse.jface.viewers Viewer TreeViewer LabelProvider ITreeContentProvider IStructuredContentProvider)))

(def viewer (atom nil))

(def tree  {:name ""
            :children [{:name "me@example.com" :children ["Inbox" "Drafts" "Sent"]}
                       {:name "other@example.com" :children ["Inbox"]}]})

(defn to-node
  "Converts the given item into a tree node with the given parent"
  [parent child]
  (if (isa? (class child) String)
    {:name child :parent parent}
    (assoc child :parent parent)))

(defn get-children 
  "Returns the children of the given parent tree node"
  [parent]
  (to-array (map (partial to-node parent) (or (:children parent) []))))

(defn make-content-provider 
  "Builds a content provider for trees structured as clojure maps/arrays"
  []
  (proxy [IStructuredContentProvider ITreeContentProvider] []
    (inputChanged [viewer old-input new-input])
    (dispose [])
    (getElements [parent] (get-children parent))
    (getParent [child] (:parent child))
    (getChildren [parent] (get-children parent))
    (hasChildren [parent] (not (nil? (:children parent))))))

(defn make-view-label-provider 
  "Provides labels and images for tree nodes"
  []
  (proxy [LabelProvider] []
    (getText [obj] (if (isa? String obj) obj (:name obj)))
    (getImage [obj]
      (.getImage (.getSharedImages (PlatformUI/getWorkbench))
        (if (nil? (:children obj))
          ISharedImages/IMG_OBJ_ELEMENT
          ISharedImages/IMG_OBJ_FOLDER)))))

(defn -createPartControl 
  "Creates the tree view control with the given parent control"
  [this parent]
  (reset! viewer (TreeViewer. parent (reduce bit-or [SWT/MULTI SWT/H_SCROLL SWT/V_SCROLL SWT/BORDER])))
  (doto @viewer
    (.setContentProvider (make-content-provider))
    (.setLabelProvider (make-view-label-provider))
    (.setInput tree)))

(defn -setFocus
  "Gives this control focus"
  [this]
  (.setFocus (.getControl @viewer)))
