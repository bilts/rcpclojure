(ns rcpclojure.widgets  
  (:use
    rcpclojure.interop
    rcpclojure.rcp
    clojure.contrib.core)
  (:import 
    (org.eclipse.jface.resource JFaceResources)
    (org.eclipse.jface.dialogs MessageDialog)
    (org.eclipse.swt SWT)
    (org.eclipse.swt.events SelectionAdapter)
    (org.eclipse.swt.layout GridData GridLayout)
    (org.eclipse.swt.widgets Composite Label Link Text)))

(defn get-font 
  "Retrieves the font from the font registry."
  [font]
  (let [registry (JFaceResources/getFontRegistry)
        default JFaceResources/DEFAULT_FONT]
    (cond
      (= font :bold) (.getBold registry default)
      (= font :italic) (.getItalic registry default)
      :else (.get registry default))))

(defn show-info 
  "Opens an info dialog with the given window label and message"
  [shell label message]
  (MessageDialog/openInformation shell label message))

(defn show-error
  "Logs the given error message and then opens a dialog displaying it to the user"
  ([window message]
    (show-error message nil nil))
  ([window message e & diagnostics]
    (log-error (str "User-visible error: \"" message "\" " diagnostics) e)
    (MessageDialog/openError (.getShell window) "Error" message)))

(def swt-constants 
  {:wrap SWT/WRAP
   :multi-line SWT/MULTI})

(def alignments {:left SWT/LEFT :right SWT/RIGHT
                 :top SWT/TOP :bottom SWT/BOTTOM 
                 :center SWT/CENTER :fill SWT/FILL})

(defn css-expand
  "Expands the given list in the manner CSS does, for instance [1] becomes
  [1 1 1 1], [1 2] becomes [1 2 1 2], and [1 2 3 4] remains unchanged."
  [args]
  (when args
    (let [[top right bottom left] args]
      [top (or right top) (or bottom top) (or left right top)])))

(defn css-align
  "Transforms a list of two alignments as described below.  When faced with
  ambiguities, prefers not to alter the arguments.
  args = [alignment] -> [alignment alignment]
  args = [h-align v-align] -> [h-align v-align]
  args = [v-align h-align] -> [h-align v-align]"
  [args]
  (when args
    (let [a (first args)
          b (or (second args) a)]
      (if (or (contains? #(:top :bottom) a) (contains? #(:left :right) b))
        [b a]
        [a b]))))

(defn bit-mask
  "If bits is seqable, returns the bitwise or of all values in the seq, 
  otherwise returns bits."
  [bits]
  (if (seqable? bits) 
    (reduce bit-or 0 bits) 
    bits))

(defn get-style-mask
  "Given a map containing style information, returns the SWT bitmask for that style."
  [style]
  (let [defaults {:wrap true}
        swt-style (merge defaults style)]
    (bit-mask (map second (filter #(contains? swt-style (first %)) swt-constants)))))

(defn make-grid-layout
  "Creates the grid layout described in the :margin key of props."
  [props]
  (set-fields! (GridLayout.) (assoc props :margin (css-expand (:margin props)))
    [marginTop :margin #(nth % 0)]
    [marginRight :margin #(nth % 1)]
    [marginBottom :margin #(nth % 2)]
    [marginLeft :margin #(nth % 3)]))

(defn make-grid-data 
  "Creates the grid data described in the :align, :fill, :size, and :span keys of props."
  [props]
  (set-fields! (GridData.) props
    [horizontalAlignment :align #((first (css-align %)) alignments)]
    [verticalAlignment :align #((second (css-align %)) alignments)]
    [grabExcessHorizontalSpace :align #(= (first (css-align %)) :fill)]
    [grabExcessVerticalSpace :align #(= (second (css-align %)) :fill)]
    [widthHint :size first]
    [heightHint :size second]
    [horizontalSpan :span first]
    [verticalSpan :span second]))

(defn set-grid! 
  "Sets the grid layout of the given item according to the values found in props"
  [item props]
  (.setLayout item (make-grid-layout props))
  (.setLayoutData item (make-grid-data props)))

(defmacro new-widget 
  "Creates a new widget with the given class, parent, and style"
  [c parent style]
  `(new ~c ~parent (get-style-mask ~style)))

(defn make-widget
  "Given a widget and properties, sets the values on the widget associated
  with those properties"
  [widget props]
  (let [value (:value props)
        style (:style props)
        layout (:layout props)]
    (if value
      (.setText widget value))
    (if (:font style) 
      (.setFont widget (get-font (:font style))))
    (if layout
      (.setLayoutData widget (make-grid-data layout)))
    (assoc props :widget widget)))

(defmacro add-children 
  "Given an element and a list of expressions for building children, builds
  those children with parent set to element."
  [element children]
  (let [par (gensym)]
    `(let [el# ~element
           ~par (:widget el#)] 
       (assoc el# 
         :children [~@(for [child children] 
                        `(-> ~par ~child))]))))

(defmacro defwidget 
  "Defines a widget-building function with the given name, which builds a widget
  of the given class.  The built widget is bound to the first value in args and
  passed to the given body statements."
  [name class args & body]
  (let [f (gensym)]
    `(do
       (defn ~f ~args ~@body)
       (defmacro ~name [parent# props# & children#]
         (list ~f (list 'add-children
                    (list 'make-widget (list 'new-widget ~class parent# (list :style props#)) props#)
                    children#))))))

; Wrapper for the swt Text widget
(defwidget text Text [widget] 
  widget)

; Wrapper for the swt Link widget
(defwidget link Link [widget]
  widget)

; Wrapper for the swt Label widget
(defwidget label Label [widget]
  widget)

; Wrapper for the swt Composite widget
(defwidget composite Composite [widget]
  (doto (:widget widget) 
    (.setLayout (make-grid-layout (:layout widget)))))

(defn on-select
  "Adds the function f as a selection listener for the given widget."
  [widget f]
  (.addSelectionListener widget 
     (proxy [SelectionAdapter] []
       (widgetSelected [e] (f)))))
