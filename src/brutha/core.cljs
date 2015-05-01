(ns brutha.core
  (:require cljsjs.react))

(defprotocol IShouldUpdate
  (should-update? [this value next-value]))

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
       (if (satisfies? IShouldUpdate behavior)
         (fn [next-props _]
           (this-as this
             (should-update? behavior (.. this -props -value) (.-value next-props))))
         (fn [next-props _]
           (this-as this
             (not= (.. this -props -value) (.-value next-props)))))
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
             (will-update behavior (.. this -props -value) (.-value next-props))))
         (fn [_]))
       :componentDidUpdate
       (if (satisfies? IDidUpdate behavior)
         (fn [prev-props _]
           (this-as this
             (did-update behavior (.. this -props -value) (.-value prev-props))))
         (fn [_ _]))
       :render
       (if (satisfies? IRender behavior)
         (fn [] (this-as this (render behavior (.. this -props -value))))
         (fn [] (this-as this (behavior (.. this -props -value)))))})

(defn- react-factory [behavior]
  (.createFactory js/React (.createClass js/React (react-methods behavior))))

(defn component [behavior]
  {:pre [(or (fn? behavior) (satisfies? IRender behavior))]}
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
