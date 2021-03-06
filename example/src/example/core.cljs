(ns example.core
  (:require [brutha.core :as br]
            [flupot.dom :as dom]
            [goog.string :as gstr]
            [goog.string.format]))

(enable-console-print!)

(def unmount-component
  (br/component
   (reify
     br/IRender
     (render [_ value] (dom/p (:foo value)))
     br/IWillUnmount
     (will-unmount [_ value node] (prn [:will-unmount value node])))))

(def time-component
  (br/component
   'Time
   (fn [dt]
     (dom/span {:class "time" :style {:color "#900"}}
       (gstr/format "%02d:%02d:%02d.%03d"
                    (.getHours dt)
                    (.getMinutes dt)
                    (.getSeconds dt)
                    (.getMilliseconds dt))))))

(let [app1 (.getElementById js/document "app1")
      app2 (.getElementById js/document "app2")]
  (defn render-time []
    (br/mount (dom/p "Time: " (time-component (js/Date.))) app1)
    (br/mount (dom/p "Copyright 2015") app2)
    (js/setTimeout render-time 16)))

(render-time)

(let [app3 (.getElementById js/document "app3")]
  (br/mount (unmount-component {:foo "bar"}) app3)
  (js/setTimeout #(br/unmount app3) 5000))
