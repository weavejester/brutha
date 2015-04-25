(ns example.core
  (:require [brutha.core :as brutha]
            [sablono.core :as html :refer-macros [html]]))

(defn foo [text owner]
  (reify
    brutha/Render
    (-render [_]
      (html [:p text]))))

(brutha/render (brutha/build foo "Hello World")
               (.getElementById js/document "app"))
