# Brutha

A simple and functional ClojureScript interface to [React][].

[react]: https://facebook.github.io/react/

## Rationale

Unlike [Om][] and [Reagent][], Brutha is unopinionated on how you
handle your application state. It doesn't include cursors or
specialized atoms, instead relying on you to call a `mount` function
with new state data.

However, Brutha *is* opinionated on how to interface with
React. Brutha components do not have their own state, and do not have
access to the underlying React component object. While this may seem
restrictive, it vastly simplifies the API without losing much
functionality.

[om]: https://github.com/omcljs/om
[reagent]: https://github.com/reagent-project/reagent

## Installation

Add the following to your project `:dependencies`:

    [brutha "0.1.0-SNAPSHOT"]

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
     br/IRender
     (render [_ value]
       (dom/p "Hello World")))))
```

A component is mounted once, and updated many times. The update
methods will not be called on the initial render.

Sometimes it's useful to specify a unique key to components. Keys
allow React to efficiently deal with updates to lists of elements. In
Brutha, you can pass `:key` in an option map to a component.

```clojure
(foo-component data {:key (:id data)})
```

## License

Copyright © 2015 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
