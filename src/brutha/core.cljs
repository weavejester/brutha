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

(def ^:dynamic *force-update* false)

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
         (fn [] (this-as this
                 (let [props (.-props this)]
                   (binding [*force-update* (.-forceUpdate props)]
                     (render behavior (.-value props))))))
         (fn [] (this-as this
                 (let [props (.-props this)]
                   (binding [*force-update* (.-forceUpdate props)]
                     (behavior (.-value props)))))))})

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
                     :forceUpdate *force-update*})))))

(def ^:private refresh-queued (atom #{}))

(def ^:private req-anim-frame
  (if (exists? js/requestAnimationFrame)
    js/requestAnimationFrame
    (fn [f] (js/setTimeout f 16))))

(defn mount [element node]
  (when-not (@refresh-queued node)
    (swap! refresh-queued conj node)
    (req-anim-frame (fn []
                      (swap! refresh-queued disj node)
                      (js/React.render element node)))))
