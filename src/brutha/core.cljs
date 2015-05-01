(ns brutha.core
  (:require cljsjs.react))

(defprotocol IWillMount
  (will-mount [this value]))

(defprotocol IDidMount
  (did-mount [this value node]))

(defprotocol IWillUpdate
  (will-update [this value next-value]))

(defprotocol IDidUpdate
  (did-update [this value prev-value]))

(defprotocol IRender
  (render [this value]))

(defn- react-methods [behavior]
  #js {:shouldComponentUpdate
       (fn [next-props _]
         (this-as this
           (not= (.. this -props -value) (.-value next-props))))
       :componentWillMount
       (fn []
         (this-as this
           (when (satisfies? IWillMount behavior)
             (will-mount behavior (.. this -props -value)))))
       :componentDidMount
       (fn []
         (this-as this
           (when (satisfies? IDidMount behavior)
             (did-mount behavior (.. this -props -value) (.getDOMNode this)))))
       :componentWillUpdate
       (fn [next-props _]
         (this-as this
           (when (satisfies? IWillUpdate behavior)
             (will-update behavior (.. this -props -value) (.-value next-props)))))
       :componentDidUpdate
       (fn [prev-props _]
         (this-as this
           (when (satisfies? IDidUpdate behavior)
             (did-update behavior (.. this -props -value) (.-value prev-props)))))
       :render
       (fn []
         (this-as this
           (render behavior (.. this -props -value))))})

(defn- react-factory [behavior]
  (.createFactory js/React (.createClass js/React (react-methods behavior))))

(defn component [behavior]
  (let [factory (react-factory behavior)]
    (fn [value]
      (factory #js {:value value}))))

(def ^:private refresh-queued #js {})

(def ^:private req-anim-frame
  (if (exists? js/requestAnimationFrame)
    js/requestAnimationFrame
    (fn [f] (js/setTimeout f 16))))

(defn mount [element node]
  (when-not (aget refresh-queued node)
    (aset refresh-queued node true)
    (req-anim-frame #(do (js-delete refresh-queued node)
                         (.render js/React element node)))))
