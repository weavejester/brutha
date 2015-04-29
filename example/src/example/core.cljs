(ns example.core
  (:require [brutha.core :as br]
            [clojure.set :as set]
            [sablono.core :as html :refer-macros [html]]))

(def counter 0)

(defn paragraph [text owner]
  (reify
    br/IWillMount
    (will-mount [_]
      (.log js/console "will-mount"))
    br/IDidMount
    (did-mount [_]
      (.log js/console "did-mount"))
    br/IWillUpdate
    (will-update [_ v]
      (.log js/console (str "will-update: " (pr-str v))))
    br/IDidUpdate
    (did-update [_ v]
      (.log js/console (str "did-update: " (pr-str v))))
    br/IRender
    (render [_]
      (set! counter (inc counter))
      (html [:div [:p text] [:p counter]]))))

(let [app (.getElementById js/document "app")]
  (defn render-time []
    (br/mount (br/build paragraph (str (.getTime (js/Date.)))) app)
    (js/setTimeout render-time 1000)))

(render-time)
