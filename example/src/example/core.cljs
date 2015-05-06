(ns example.core
  (:require [brutha.core :as br]
            [flupot.dom :as dom]
            [goog.string :as gstr]
            [goog.string.format]))

(def time-component
  (br/component
   (fn [dt]
     (dom/span {:class "time" :style {:color "#900"}}
       (gstr/format "%02d:%02d:%02d.%03d"
                    (.getHours dt)
                    (.getMinutes dt)
                    (.getSeconds dt)
                    (.getMilliseconds dt))))))

(let [app (.getElementById js/document "app")]
  (defn render-time []
    (br/mount (dom/p "Time: " (time-component (js/Date.))) app)
    (js/setTimeout render-time 16)))

(render-time)
