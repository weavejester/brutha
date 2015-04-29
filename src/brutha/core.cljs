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
             (render ((.-behavior props) (.-value props) this)))))})

(def ^:private react-factory
  (.createFactory js/React (.createClass js/React react-methods)))

(defn build [behavior value]
  (react-factory #js {:behavior behavior, :value value}))

(defn mount [element node]
  (.render js/React element node))
