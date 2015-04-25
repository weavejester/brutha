(ns example.core
  (:require [brutha.core :as brutha]
            [sablono.core :as html :refer-macros [html]]))

(defn foo [text owner]
  (reify
    brutha/Render
    (-render [_]
      (html [:p text]))))

(let [f (brutha/build foo)]
  (brutha/render (f "Hello World") (.getElementById js/document "app")))
