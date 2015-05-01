(ns example.core
  (:require [brutha.core :as br]
            [clojure.set :as set]
            [sablono.core :as html :refer-macros [html]]))

(def counter 0)

(def paragraph
  (br/component
   (reify
     br/IShouldUpdate
     (should-update? [_ a b]
       (.log js/console "should-update?")
       (not= a b))
     br/IWillMount
     (will-mount [_ _]
       (.log js/console "will-mount"))
     br/IDidMount
     (did-mount [_ _ node]
       (.log js/console "did-mount")
       (.log js/console node))
     br/IWillUpdate
     (will-update [_ _ v node]
       (.log js/console (str "will-update: " (pr-str v)))
       (.log js/console node))
     br/IDidUpdate
     (did-update [_ _ v node]
       (.log js/console (str "did-update: " (pr-str v)))
       (.log js/console node))
     br/IRender
     (render [_ text]
       (set! counter (inc counter))
       (html [:div [:p text] [:p counter]])))))

(let [app (.getElementById js/document "app")]
  (defn render-time []
    (br/mount
     (html [:div (paragraph (str (.getTime (js/Date.))) {:key "date"})])
     app)
    (js/setTimeout render-time 1000)))

(render-time)
