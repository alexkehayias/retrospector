(ns retrospector.app
  (:use [retrospector.utils.logging :only [debug info error]])
  (:use-macros [dommy.macros :only [node sel sel1]])
  (:require [clojure.browser.repl :as repl]
            [dommy.core :as dom]))

(def RENDERER (atom {}))

(def GRID (atom []))

(def HOSTNAME "127.0.0.1")
(def PORT "9000")

(def app-main
  [:div#main])

(defn get-window-dimensions
  "Returns the height and width of the window"
  []
  (map #(aget js/window %) ["innerWidth" "innerHeight"]))

(defn make-grid!
  "Generate points for the top left corner of a grid of x y dimensions
  as a collection of x, y coordinance"
  [w h dx dy]
  (reset! GRID (for [x (range 0 w (js/parseInt dx))]
                 (for [y (range 0 h (js/parseInt dy))]
                   (let [graphic (new js/PIXI.Graphics)]
                     (.addChild (:stage @RENDERER) graphic)
                     {:x x :y y :w dx :h dy
                      :graphic graphic
                      :color "#000000"})))))

(defn init-html! []
  (info "Initializing base html")
  (dom/append! (sel1 :body) app-main))

(defn init-renderer!
  []
  (try (dom/remove! (sel1 :canvas))
       (catch js/Object e (error (str e))))
  (let [[width height] (get-window-dimensions)
        stage (new js/PIXI.Stage 0xFFFFFF)
        renderer (new js/PIXI.autoDetectRenderer width height)]
    (dom/append! (sel1 :body) (aget renderer "view"))
    (reset! RENDERER {:stage stage
                      :renderer renderer
                      :width width
                      :height height})))

(defn str->hexcode [s]
  (js/parseInt (.replace s "#" "") 16))


(defn rect [graphic x y h w color]
  (.lineStyle graphic 0)
  (.beginFill graphic (str->hexcode color))
  (.drawRect graphic x y h w)
  (.render (:renderer @RENDERER) (:stage @RENDERER)))

(defn draw-grid!
  [grid]
  (doseq [row grid
          {:keys [graphic x y w h color] :as cell} row]
    (js/setTimeout (fn [] (rect graphic x y w h (or color "#000000")))
                   (* (js/Math.random) 100))))

(defn init-grid!
  []
  (let [{:keys [width height]} @RENDERER
        grid-width (/ width (* 8 4))
        grid-height (/ height (* 5 4))
        grid (make-grid! width height grid-width grid-height)]
    (draw-grid! grid)))

(defn change-colors
  "Change the grid to match the colors in color-grid
  Example color-grid:
  [[\"#ffffff\" \"#ffffff\" \"#ffffff\"]
   [\"#ffffff\" \"#ffffff\" \"#ffffff\"]
   [\"#ffffff\" \"#ffffff\" \"#ffffff\"]]"
  [grid color-grid]
  (for [[grid-row color-row] (map #(into [] [%1 %2]) grid color-grid)]
    (for [[cell color] (map vector grid-row color-row)]
      (assoc cell :color (or color "#000000")))))

(defn swap-to-colors!
  [color-grid]
  (debug "Swapping colors")
  (draw-grid! (reset! GRID (change-colors @GRID color-grid))))

(defn reset-app!
  "Reload the entire application html and canvas app"
  []
  (info "Resetting html")
  (try (do (dom/remove! (sel1 :#top-nav))
           (dom/remove! (sel1 :#main)))
       (catch js/Error e (error e)))
  (init-html!)
  (init-renderer!)
  (init-grid!))

(defn load-colors-callback [response]
  (let [resp (js->clj (.getResponseJson (.-target response)))]
    (swap-to-colors! resp)))

(def keycodes
  {38 "x" ; :up
   39 "y" ; :right
   40 "z" ; :down
   })

(def STATE (atom {}))

(defn make-request
  "Make an async call to the server."
  [field offset limit]
  (debug "Getting stars" field offset limit)
  (.send goog.net.XhrIo
         (str "http://" HOSTNAME ":" PORT
              "/api/v1/stars?field=" field
              "&offset=" offset "&limit=" limit)
         load-colors-callback)
  (swap! STATE #(assoc % "x" (+ (get % "x") 100))))

(defn keyboard-handler
  [e]
  (.preventDefault e)
  (let [k (get keycodes (aget e "keyCode"))]
    (when k
      (make-request k (+ (get @STATE k 100) 100) 100))))

(defn reset-app!
  "Reload the entire application html and canvas app"
  []
  (info "Resetting html")
  (try (do (dom/remove! (sel1 :#top-nav))
           (dom/remove! (sel1 :#main)))
       (catch js/Error e (error e)))
  (init-html!)
  (init-renderer!)
  (init-grid!)
  (dom/unlisten! (sel1 :body) :keydown keyboard-handler)
  (dom/listen! (sel1 :body) :keydown keyboard-handler))

;; Start the game on page load
;; (set! (.-onload js/window) reset-app!)
(reset-app!)
