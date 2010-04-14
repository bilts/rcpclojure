; Utilities for Java interop
(ns rcpclojure.interop)

(defmacro set-props! 
  "For each provided setter of the form [setFunc :key transform], calls
   (.setFunc obj (transform (:key props))) when :key is found in props.
   transform can be omitted, in which case the transformation is the
   identity transformation."
  [obj props & setters]
  (let [o (gensym)]
    `(let [~o ~obj]
       ~@(for [[setter key func] setters] 
          `(if (contains? ~props ~key) 
             (. ~o ~setter ((or ~func identity) (~key ~props)))))
       ~o)))

(defmacro set-fields!
  "For each provided setter of the form [field :key transform], calls
   (set! (. obj field) (transform (:key props))) when :key is found in props.
   transform can be omitted, in which case the transformation is the
   identity transformation."  
  [obj props & fields]
  (let [o (gensym)]
    `(let [~o ~obj]
       ~@(for [[field key func] fields] 
           `(if (contains? ~props ~key) 
              (set! (. ~o ~field) ((or ~func identity) (~key ~props)))))
       ~o)))

(defmacro with-id 
  "Executes the body expressions with id-binding bound to a numeric ID 
   guaranteed to be unique for each execution."
  [id-binding & body]
  `(let [~id-binding (atom 0)]
     (swap! ~id-binding inc)
     ~@body))

(defn add!
  "Calls (.add obj value) for each value passed."
  [obj & values]
  (doseq [value values]
    (.add obj value))
  obj)

(defn add-keys! [obj map & keys]
  "For each key passed, looks up the key in the map and calls (.add obj value)"
  (doseq [key keys]
    (.add obj (key map)))
  obj)

(defmacro #^{:private true} assert-args [fnname & pairs]
  "Copied from private clojure.core macro"
  `(do (when-not ~(first pairs)
         (throw (IllegalArgumentException.
                  ~(str fnname " requires " (second pairs)))))
     ~(let [more (nnext pairs)]
        (when more
          (list* `assert-args fnname more)))))

(defmacro with-cleanup
  "bindings => [name init ...]

  Largely copied from clojure.core with-open
 
  Evaluates body in a try expression with names bound to the values
  of the inits, and a finally clause that calls the java cleanup method
  on each name in reverse order."
  [bindings cleanup-method & body]
  (assert-args with-cleanup
     (vector? bindings) "a vector for its binding"
     (even? (count bindings)) "an even number of forms in binding vector")
  (cond
    (= (count bindings) 0) `(do ~@body)
    (symbol? (bindings 0)) `(let ~(subvec bindings 0 2)
                              (try
                                (with-cleanup ~(subvec bindings 2) ~cleanup-method ~@body)
                                (finally
                                  (~cleanup-method ~(bindings 0)))))
    :else (throw (IllegalArgumentException.
                   "with-cleanup only allows Symbols in bindings"))))

(defmacro with-disposable
  "Evaluates body in a try expression with names bound to the values of the
   inits, and a finally clause that calls .dispose on each bound value in
   reverse order."
  [bindings & body]
  `(with-cleanup ~bindings .dispose ~@body))
