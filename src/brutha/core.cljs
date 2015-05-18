(ns brutha.core
  (:require cljsjs.react))

(defprotocol IShouldUpdate
  (should-update? [this value next-value]))

(defprotocol IWillMount
  (will-mount [this value]))

(defprotocol IDidMount
  (did-mount [this value node]))

(defprotocol IWillUpdate
  (will-update [this value next-value node]))

(defprotocol IDidUpdate
  (did-update [this value prev-value node]))

(defprotocol IRender
  (render [this value]))

(def ^:dynamic *force-update?* false)

(defn- react-methods [behavior]
  #js {:shouldComponentUpdate
       (if (satisfies? IShouldUpdate behavior)
         (fn [next-props _]
           (this-as this
             (or (.-forceUpdate next-props)
                 (should-update? behavior (.. this -props -value) (.-value next-props)))))
         (fn [next-props _]
           (this-as this
             (or (.-forceUpdate next-props)
                 (not= (.. this -props -value) (.-value next-props))))))
       :componentWillMount
       (if (satisfies? IWillMount behavior)
         (fn [] (this-as this (will-mount behavior (.. this -props -value))))
         (fn []))
       :componentDidMount
       (if (satisfies? IDidMount behavior)
         (fn []
           (this-as this
             (did-mount behavior (.. this -props -value) (.getDOMNode this))))
         (fn []))
       :componentWillUpdate
       (if (satisfies? IWillUpdate behavior)
         (fn [next-props]
           (this-as this
             (let [value (.. this -props -value)]
               (will-update behavior value (.-value next-props) (.getDOMNode this)))))
         (fn [_]))
       :componentDidUpdate
       (if (satisfies? IDidUpdate behavior)
         (fn [prev-props _]
           (this-as this
             (let [value (.. this -props -value)]
               (did-update behavior value (.-value prev-props) (.getDOMNode this)))))
         (fn [_ _]))
       :render
       (if (satisfies? IRender behavior)
         (fn [] (this-as this (render behavior (.. this -props -value))))
         (fn [] (this-as this (behavior (.. this -props -value)))))})

(defn- react-factory [behavior]
  (js/React.createFactory (js/React.createClass (react-methods behavior))))

(defn component
  [behavior]
  {:pre [(or (fn? behavior) (satisfies? IRender behavior))]}
  (let [factory (react-factory behavior)]
    (fn create-element
      ([value]
       (create-element value {}))
      ([value opts]
       (factory #js {:key (opts :key js/undefined)
                     :value value
                     :forceUpdate *force-update?*})))))

(def ^:private refresh-queued #js {})

(def ^:private req-anim-frame
  (if (exists? js/requestAnimationFrame)
    js/requestAnimationFrame
    (fn [f] (js/setTimeout f 16))))

(defn mount [element node]
  (when-not (aget refresh-queued node)
    (aset refresh-queued node true)
    (req-anim-frame (fn []
                      (js-delete refresh-queued node)
                      (js/React.render element node)))))
