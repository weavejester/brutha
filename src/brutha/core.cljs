(ns brutha.core
  (:require cljsjs.react))

(defprotocol Render
  (-render [this]))

(defn- react-methods [component]
  {:shouldComponentUpdate
   (fn [next-props _]
     (this-as this
       (not= (-> this .-props .-value) (.-value next-props))))
   :render
   (fn []
     (this-as this
       (-render (component (-> this .-props .-value) this))))})

(defn build [component]
  (let [methods    (react-methods component)
        react-comp (.createClass js/React (clj->js methods))]
    (fn [value]
      (.createElement js/React react-comp #js {:value value}))))

(defn render [element node]
  (.render js/React element node))
