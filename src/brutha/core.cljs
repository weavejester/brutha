(ns brutha.core
  (:require cljsjs.react))

(defprotocol IRender
  (render [this]))

(def ^:private react-methods
  #js {:shouldComponentUpdate
       (fn [next-props _]
         (this-as this
           (not= (-> this .-props .-value) (.-value next-props))))
       :render
       (fn []
         (this-as this
           (let [props (.-props this)]
             (render (.behavior props (.-value props) this)))))})

(def ^:private react-factory
  (.createFactory js/React (.createClass js/React react-methods)))

(defn build [behavior value]
  (react-factory #js {:behavior behavior, :value value}))

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
