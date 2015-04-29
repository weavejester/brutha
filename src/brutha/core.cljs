(ns brutha.core
  (:require cljsjs.react))

(defprotocol IWillMount
  (will-mount [this]))

(defprotocol IDidMount
  (did-mount [this]))

(defprotocol IWillUpdate
  (will-update [this next-value]))

(defprotocol IDidUpdate
  (did-update [this prev-value]))

(defprotocol IRender
  (render [this]))

(def ^:private react-methods
  #js {:shouldComponentUpdate
       (fn [next-props _]
         (this-as this
           (not= (-> this .-props .-value) (.-value next-props))))
       :componentWillMount
       (fn []
         (this-as this
           (let [props    (.-props this)
                 behavior (.behave props (.-value props) this)]
             (aset this "__brutha_behavior" behavior)
             (when (satisfies? IWillMount behavior)
               (will-mount behavior)))))
       :componentDidMount
       (fn []
         (this-as this
           (let [behavior (aget this "__brutha_behavior")]
             (when (satisfies? IDidMount behavior)
               (did-mount behavior)))))
       :componentWillReceiveProps
       (fn [next-props]
         (this-as this
           (let [behavior (.behave next-props (.-value next-props) this)]
             (aset this "__brutha_behavior" behavior))))
       :componentWillUpdate
       (fn [next-props _]
         (this-as this
           (let [behavior (aget this "__brutha_behavior")]
             (when (satisfies? IWillUpdate behavior)
               (will-update behavior (.-value next-props))))))
       :componentDidUpdate
       (fn [prev-props _]
         (this-as this
           (let [behavior (aget this "__brutha_behavior")]
             (when (satisfies? IDidUpdate behavior)
               (did-update behavior (.-value prev-props))))))
       :render
       (fn []
         (this-as this
           (render (aget this "__brutha_behavior"))))})

(def ^:private react-factory
  (.createFactory js/React (.createClass js/React react-methods)))

(defn build [behave value]
  (react-factory #js {:behave behave, :value value}))

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
