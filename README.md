# Brutha

A simple and functional ClojureScript interface to [React][].

[react]: https://facebook.github.io/react/

## Rationale

Unlike [Om][] and [Reagent][], Brutha is unopinionated on how you
handle your application state. It doesn't include cursors or
specialized atoms, instead relying on you to call a `mount` function
with new state data. This is useful when you want to manage your
application state yourself.

[om]: https://github.com/omcljs/om
[reagent]: https://github.com/reagent-project/reagent

## Installation

Add the following to your project `:dependencies`:

    [brutha "0.2.1"]

## Usage

Brutha doesn't include a `dom` namespace like Om, so you'll need to
use a library like [Flupot][], [Sablono][] or [Kioo][]. In the
examples we'll use Flupot.

[flupot]: https://github.com/weavejester/flupot
[sablono]: https://github.com/r0man/sablono
[kioo]: https://github.com/ckirkendall/kioo

First we'll require Brutha and Flupot:

```clojure
(ns brutha.example
  (:require [brutha.core :as br]
            [flupot.dom :as dom]))
```

You can use the `brutha.core/mount` function to attach a React element
to a DOM node.

```clojure
(def app (js/document.getElementById "app"))

(br/mount (dom/p "Hello World") app)
```

When you want to update, just call the `mount` function again. React
will efficiently work out what needs to be changed and update only
those elements.

```clojure
(br/mount (dom/p "Goodbye World") app)
```

If you want to remove the associated component from a DOM node, use
the `brutha.core/unmount` function:

```clojure
(br/unmount app)
```

A Brutha component is a pure function that takes in an immutable data
structure, and returns a React element. The most straightforward way
to write a component is to pass a function to `brutha.core/component`:

```clojure
(def unixtime
  (br/component (fn [date] (dom/p (str (.getTime date))))))

(br/mount (unixtime (js/Date.)) app)
```

By wrapping the function in a component, React knows only to update
the DOM when the value passed to the function changes.

For debugging purposes, particularly when working with
[React Developer Tools][devtools], it often helps to give a component
a display name by passing an extra argument when creating the
component.

```clojure
(def unixtime
  (br/component 'UnixTime (fn [date] (dom/p (str (.getTime date))))))
```

[devtools]: https://github.com/facebook/react-devtools

When using the same component multiple times with a collection of
data, it's important to give React a key to identify the
component. You can do this by passing a `:key` option to the
component:

```clojure
(foo-component data {:key (:id data)})
```

Sometimes it's useful to know when a component is mounted onto the
DOM. Brutha supports this too. Instead of supplying a function to
`component`, you can supply a reified type:

```clojure
(def noisy-component
  (br/component
   (reify
     br/IShouldUpdate
     (should-update? [_ a b]
       (not= a b))

     br/IWillMount
     (will-mount [_ value]
       (js/console.log "will-mount"))

     br/IDidMount
     (did-mount [_ value dom-node]
       (js/console.log "did-mount"))

     br/IWillUpdate
     (will-update [_ value next-value dom-node]
       (js/console.log "will-update"))

     br/IDidUpdate
     (did-update [_ value prev-value dom-node]
       (js/console.log "did-update"))

     br/IWillUnmount
     (will-unmount [_ value dom-node]
       (js/console.log "will-unmount"))

     br/IRender
     (render [_ value]
       (dom/p "Hello World")))))
```

A component is mounted once, and updated many times. The update
methods will not be called on the initial render.


## License

Copyright © 2016 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
