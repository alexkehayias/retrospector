(ns retrospector.stars)

;; Based on input data translate to colors

(defn scale-to-255
  "Scale to a number between 1 and 255 based on the upwards and lower bounds"
  [n max-n min-n]
  (js/parseInt (/ (* (- n min-n) (- 255 1)) (+ (- max-n min-n) 1))))

(defn luminosity->blue
  "Sets the blue level"
  [lum]
  (scale-to-255 lum 4438.1123 -4670.2939))

(defn velocity->red
  [vel]
  (scale-to-255 vel 76.92 0))

(defn density->green
  [count]
  (scale-to-255 count 769.019 -580.666))
