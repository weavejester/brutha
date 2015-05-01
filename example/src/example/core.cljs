(ns example.core
  (:require [brutha.core :as br]
            [clojure.set :as set]
            [sablono.core :as html :refer-macros [html]]))

(def counter 0)

(def paragraph
  (br/component
   (fn [text]
     (set! counter (inc counter))
     (html [:div [:p text] [:p counter]]))))

(let [app (.getElementById js/document "app")]
  (defn render-time []
    (br/mount (paragraph (str (.getTime (js/Date.)))) app)
    (js/setTimeout render-time 16)))

(render-time)
