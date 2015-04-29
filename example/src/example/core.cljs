(ns example.core
  (:require [brutha.core :as br]
            [sablono.core :as html :refer-macros [html]]))

(defn foo [text owner]
  (reify
    br/IRender
    (render [_]
      (html [:p text]))))

(br/mount (br/build foo "Hello World")
          (.getElementById js/document "app"))
