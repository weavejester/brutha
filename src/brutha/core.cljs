(ns brutha.core
  (:require cljsjs.react.dom))

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

(defprotocol IWillUnmount
  (will-unmount [this value node]))

(defprotocol IRender
  (render [this value]))

(defn- react-options [display-name behavior]
  #js {:displayName display-name
       :shouldComponentUpdate
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
             (did-mount behavior (.. this -props -value) (js/ReactDOM.findDOMNode this))))
         (fn []))
       :componentWillUpdate
       (if (satisfies? IWillUpdate behavior)
         (fn [next-props]
           (this-as this
             (let [value (.. this -props -value)]
               (will-update behavior value (.-value next-props) (js/ReactDOM.findDOMNode this)))))
         (fn [_]))
       :componentDidUpdate
       (if (satisfies? IDidUpdate behavior)
         (fn [prev-props _]
           (this-as this
             (let [value (.. this -props -value)]
               (did-update behavior value (.-value prev-props) (js/ReactDOM.findDOMNode this)))))
         (fn [_ _]))
       :componentWillUnmount
       (if (satisfies? IWillUnmount behavior)
         (fn []
           (this-as this
             (will-unmount behavior (.. this -props -value) (js/ReactDOM.findDOMNode this))))
         (fn []))
       :render
       (if (satisfies? IRender behavior)
         (fn [] (this-as this
                 (render behavior (-> this .-props .-value))))
         (fn [] (this-as this
                 (behavior (-> this .-props .-value)))))})

(defn- react-factory [display-name behavior]
  (js/React.createFactory (js/React.createClass (react-options display-name behavior))))

(defn component
  ([behavior]
   (component nil behavior))
  ([display-name behavior]
   {:pre [(or (fn? behavior) (satisfies? IRender behavior))]}
   (let [factory (react-factory (str display-name) behavior)]
     (fn create-element
       ([value]
        (create-element value {}))
       ([value opts]
        (if-let [key (:key opts)]
          (factory #js {:value value, :key key})
          (factory #js {:value value})))))))

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
                      (js/ReactDOM.render element node)))))

(defn unmount [node]
  (js/ReactDOM.unmountComponentAtNode node))
